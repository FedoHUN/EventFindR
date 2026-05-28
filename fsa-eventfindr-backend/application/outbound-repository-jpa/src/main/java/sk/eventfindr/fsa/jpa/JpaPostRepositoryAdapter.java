package sk.eventfindr.fsa.jpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaPostRepositoryAdapter implements PostRepository {

    private final PostSpringDataRepository repository;

    public JpaPostRepositoryAdapter(PostSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Post post) {
        repository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Collection<Post> findByAuthorId(Long authorId) {
        return repository.findByAuthorIdOrderByCreatedDesc(authorId);
    }

    @Override
    public Collection<Post> findByAuthorId(Long authorId, int offset, int limit) {
        int page = offset / Math.max(limit, 1);
        return repository.findByAuthorIdOrderByCreatedDesc(authorId, PageRequest.of(page, limit))
                .getContent();
    }

    @Override
    public long countByAuthorId(Long authorId) {
        return repository.countByAuthorId(authorId);
    }

    @Override
    public void delete(Post post) {
        repository.delete(post);
    }
}
