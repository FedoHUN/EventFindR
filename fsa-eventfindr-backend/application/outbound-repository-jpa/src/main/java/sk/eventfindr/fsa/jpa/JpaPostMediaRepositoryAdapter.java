package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.MediaType;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.PostMediaRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaPostMediaRepositoryAdapter implements PostMediaRepository {

    private final PostMediaSpringDataRepository repository;

    public JpaPostMediaRepositoryAdapter(PostMediaSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(PostMedia media) {
        repository.save(media);
    }

    @Override
    public Optional<PostMedia> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Collection<PostMedia> findByPostId(Long postId) {
        return repository.findByPostIdOrderBySortOrder(postId);
    }

    @Override
    public void delete(PostMedia media) {
        repository.delete(media);
    }

    @Override
    @Transactional
    public void deleteAllByPostId(Long postId) {
        repository.deleteAllByPostId(postId);
    }

    @Override
    public int countByPostIdAndMediaType(Long postId, MediaType mediaType) {
        return repository.countByPostIdAndMediaType(postId, mediaType);
    }
}
