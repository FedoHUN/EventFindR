package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.PaginatedResult;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;

import java.util.Collection;

public interface PostFacade {

    Long createPost(Long authorId, String content);

    Collection<Post> getPostsByAuthor(Long authorId);

    PaginatedResult<Post> getPostsByAuthor(Long authorId, int page, int size);

    Post getPostById(Long postId);

    void deletePost(Long postId, Long userId);

    PostMedia uploadMedia(Long postId, Long userId, String originalName,
                          String contentType, long fileSize, byte[] data);

    Collection<PostMedia> getMediaForPost(Long postId);

    byte[] getMediaFile(Long mediaId);

    void deleteMedia(Long mediaId, Long userId);
}
