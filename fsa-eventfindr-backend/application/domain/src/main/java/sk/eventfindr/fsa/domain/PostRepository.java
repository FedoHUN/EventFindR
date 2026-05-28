package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface PostRepository {

    void save(Post post);

    Optional<Post> findById(Long id);

    Collection<Post> findByAuthorId(Long authorId);

    Collection<Post> findByAuthorId(Long authorId, int offset, int limit);

    long countByAuthorId(Long authorId);

    void delete(Post post);
}
