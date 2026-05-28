package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface PostMediaRepository {

    void save(PostMedia media);

    Optional<PostMedia> findById(Long id);

    Collection<PostMedia> findByPostId(Long postId);

    void delete(PostMedia media);

    void deleteAllByPostId(Long postId);

    int countByPostIdAndMediaType(Long postId, MediaType mediaType);
}
