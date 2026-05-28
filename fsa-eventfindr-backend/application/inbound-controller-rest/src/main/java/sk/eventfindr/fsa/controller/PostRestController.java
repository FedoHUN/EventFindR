package sk.eventfindr.fsa.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.PaginatedResult;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRole;
import sk.eventfindr.fsa.domain.service.PostFacade;
import sk.eventfindr.fsa.mapper.PostMapper;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class PostRestController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp",
            "video/mp4", "video/quicktime"
    );

    private final PostFacade postFacade;
    private final PostMapper postMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public PostRestController(PostFacade postFacade,
                              PostMapper postMapper,
                              CurrentUserDetailService currentUserDetailService) {
        this.postFacade = postFacade;
        this.postMapper = postMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @PostMapping("/posts")
    public ResponseEntity<PostCreatedResponseDto> createPost(@RequestBody CreatePostRequestDto request) {
        User author = currentUserDetailService.getFullCurrentUser();
        Long postId = postFacade.createPost(author.getId(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostCreatedResponseDto(postId));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> getPostsByUser(@PathVariable("userId") Long userId,
                                            @RequestParam(value = "page", required = false) Integer page,
                                            @RequestParam(value = "size", required = false) Integer size) {

        if (page != null && size != null) {
            PaginatedResult<Post> result = postFacade.getPostsByAuthor(userId, page, size);
            return ResponseEntity.ok(new PagedPostsResponseDto(
                    postMapper.toDtoList(result.content()),
                    result.page(),
                    result.size(),
                    result.totalElements(),
                    result.totalPages(),
                    result.last()
            ));
        }

        Collection<Post> posts = postFacade.getPostsByAuthor(userId);
        return ResponseEntity.ok(postMapper.toDtoList(posts));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        User user = currentUserDetailService.getFullCurrentUser();
        postFacade.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/posts/{postId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostMediaResponseDto> uploadMedia(@PathVariable("postId") Long postId,
                                                            @RequestParam("file") MultipartFile file) {
        User user = currentUserDetailService.getFullCurrentUser();

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Failed to read uploaded file");
        }

        PostMedia media = postFacade.uploadMedia(
                postId,
                user.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toMediaResponse(postId, media));
    }

    @GetMapping("/posts/{postId}/media/{mediaId}/file")
    public ResponseEntity<byte[]> getMediaFile(@PathVariable("postId") Long postId,
                                               @PathVariable("mediaId") Long mediaId) {
        Collection<PostMedia> media = postFacade.getMediaForPost(postId);
        PostMedia target = media.stream()
                .filter(item -> item.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media not found"));

        String contentType = target.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Unsupported media content type");
        }

        byte[] fileData = postFacade.getMediaFile(mediaId);
        String disposition = contentType.startsWith("image/") ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", disposition)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(fileData);
    }

    @DeleteMapping("/posts/{postId}/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable("postId") Long postId,
                                            @PathVariable("mediaId") Long mediaId) {
        User user = currentUserDetailService.getFullCurrentUser();
        postFacade.deleteMedia(mediaId, user.getId());
        return ResponseEntity.noContent().build();
    }

    public record CreatePostRequestDto(String content) {
    }

    public record PostAuthorDto(Long id,
                                String name,
                                String organizationName,
                                String artistName,
                                UserRole role) {
    }

    public record PostMediaDto(Long id,
                               String mediaType,
                               String contentType,
                               int sortOrder,
                               String url) {
    }

    public record PostDto(Long id,
                          String content,
                          Date created,
                          Date updated,
                          int mediaCount,
                          PostAuthorDto author,
                          List<PostMediaDto> media) {
    }

    public record PostMediaResponseDto(Long id,
                                       String mediaType,
                                       String contentType,
                                       int sortOrder,
                                       String url) {
    }

    public record PostCreatedResponseDto(Long id) {
    }

    public record PagedPostsResponseDto(List<PostDto> content,
                                        int page,
                                        int size,
                                        long totalElements,
                                        int totalPages,
                                        boolean last) {
    }
}
