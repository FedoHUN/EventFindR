package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.MediaStorage;
import sk.eventfindr.fsa.domain.MediaType;
import sk.eventfindr.fsa.domain.MediaValidator;
import sk.eventfindr.fsa.domain.PaginatedResult;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.PostMediaRepository;
import sk.eventfindr.fsa.domain.PostRepository;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;
import sk.eventfindr.fsa.domain.VideoCompressor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class PostService implements PostFacade {

    private static final int MAX_IMAGES_PER_POST = 10;
    private static final int MAX_VIDEOS_PER_POST = 1;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final Set<UserRole> ALLOWED_ROLES = Set.of(UserRole.ORGANIZER, UserRole.ARTIST, UserRole.ADMIN);

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final UserRepository userRepository;
    private final MediaStorage mediaStorage;
    private final VideoCompressor videoCompressor;
    private final MediaValidator mediaValidator;
    private final Clock clock;
    private final DomainLogger log;

    public PostService(PostRepository postRepository,
                       PostMediaRepository postMediaRepository,
                       UserRepository userRepository,
                       MediaStorage mediaStorage,
                       VideoCompressor videoCompressor) {
        this(postRepository, postMediaRepository, userRepository, mediaStorage, videoCompressor,
                MediaValidator.defaultValidator(), Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public PostService(PostRepository postRepository,
                       PostMediaRepository postMediaRepository,
                       UserRepository userRepository,
                       MediaStorage mediaStorage,
                       VideoCompressor videoCompressor,
                       MediaValidator mediaValidator,
                       Clock clock,
                       DomainLogger log) {
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.userRepository = userRepository;
        this.mediaStorage = mediaStorage;
        this.videoCompressor = videoCompressor;
        this.mediaValidator = mediaValidator;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public Long createPost(Long authorId, String content) {
        User author = getRequiredUser(authorId);
        validateCanPost(author);

        String normalizedContent = validateContent(content);

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(normalizedContent);
        post.setCreated(Date.from(clock.instant()));

        postRepository.save(post);
        log.info("Created post {} by user {}", post.getId(), authorId);
        return post.getId();
    }

    @Override
    public Collection<Post> getPostsByAuthor(Long authorId) {
        Collection<Post> posts = postRepository.findByAuthorId(authorId);
        posts.forEach(this::enrichPost);
        return posts;
    }

    @Override
    public PaginatedResult<Post> getPostsByAuthor(Long authorId, int page, int size) {
        int clampedSize = Math.max(1, Math.min(size, 50));
        int safePage = Math.max(page, 0);
        int offset = safePage * clampedSize;
        Collection<Post> posts = postRepository.findByAuthorId(authorId, offset, clampedSize);
        posts.forEach(this::enrichPost);
        long total = postRepository.countByAuthorId(authorId);
        return PaginatedResult.of(posts, safePage, clampedSize, total);
    }

    @Override
    public Post getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Post was not found"));
        enrichPost(post);
        return post;
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Post was not found"));

        User user = getRequiredUser(userId);
        post.validateDeletion(user);

        Collection<PostMedia> media = postMediaRepository.findByPostId(postId);
        for (PostMedia item : media) {
            mediaStorage.delete(item.getFileName());
        }
        postMediaRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
        log.info("Deleted post {} by user {}", postId, userId);
    }

    @Override
    public PostMedia uploadMedia(Long postId, Long userId, String originalName,
                                 String contentType, long fileSize, byte[] data) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Post was not found"));

        User user = getRequiredUser(userId);
        if (!post.getAuthor().getId().equals(user.getId())) {
            log.warn("Rejected post media upload for post {} by user {}", postId, userId);
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only the post author can upload media");
        }
        validateCanPost(user);

        MediaType mediaType = mediaValidator.resolveMediaType(contentType);
        mediaValidator.validateFileSize(mediaType, fileSize);
        validateMediaSlotAvailability(postId, mediaType);

        String storedFileName = UUID.randomUUID() + mediaValidator.extractExtension(originalName, contentType);
        mediaStorage.store(data, storedFileName);

        PostMedia media = new PostMedia();
        media.setPostId(postId);
        media.setFileName(storedFileName);
        media.setOriginalName(originalName);
        media.setContentType(contentType);
        media.setMediaType(mediaType);
        media.setFileSize(fileSize);
        media.setSortOrder(postMediaRepository.findByPostId(postId).size());
        media.setCreated(Date.from(clock.instant()));

        try {
            postMediaRepository.save(media);
        } catch (RuntimeException ex) {
            mediaStorage.delete(storedFileName);
            throw ex;
        }

        if (mediaType == MediaType.VIDEO) {
            videoCompressor.compress(storedFileName);
        }

        log.info("Uploaded {} media {} for post {}", mediaType, media.getId(), postId);
        return media;
    }

    @Override
    public Collection<PostMedia> getMediaForPost(Long postId) {
        return postMediaRepository.findByPostId(postId);
    }

    @Override
    public byte[] getMediaFile(Long mediaId) {
        PostMedia media = postMediaRepository.findById(mediaId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media was not found"));
        return mediaStorage.load(media.getFileName());
    }

    @Override
    public void deleteMedia(Long mediaId, Long userId) {
        PostMedia media = postMediaRepository.findById(mediaId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media was not found"));

        Post post = postRepository.findById(media.getPostId())
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Post was not found"));

        User user = getRequiredUser(userId);
        post.validateDeletion(user);

        mediaStorage.delete(media.getFileName());
        postMediaRepository.delete(media);
        log.info("Deleted post media {} for post {}", mediaId, post.getId());
    }

    private void enrichPost(Post post) {
        Collection<PostMedia> media = postMediaRepository.findByPostId(post.getId());
        post.setMedia(new ArrayList<>(media));
        post.setMediaCount(media.size());
    }

    private User getRequiredUser(Long userId) {
        return userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));
    }

    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Post content cannot be empty");
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Post content exceeds the maximum length of " + MAX_CONTENT_LENGTH + " characters");
        }
        return normalized;
    }

    private void validateCanPost(User user) {
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            log.warn("Rejected post creation for {} because role {} cannot post", user.getEmail(), user.getRole());
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only organizers, artists, and admins can create posts");
        }
    }

    private void validateMediaSlotAvailability(Long postId, MediaType mediaType) {
        int currentCount = postMediaRepository.countByPostIdAndMediaType(postId, mediaType);
        if (mediaType == MediaType.IMAGE && currentCount >= MAX_IMAGES_PER_POST) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "A post can have at most " + MAX_IMAGES_PER_POST + " images");
        }
        if (mediaType == MediaType.VIDEO && currentCount >= MAX_VIDEOS_PER_POST) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "A post can have at most " + MAX_VIDEOS_PER_POST + " video");
        }
    }
}
