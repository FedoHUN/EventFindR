package sk.eventfindr.fsa.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.Post;

import java.util.Collection;

interface PostSpringDataRepository extends JpaRepository<Post, Long> {

    Collection<Post> findByAuthorIdOrderByCreatedDesc(Long authorId);

    Page<Post> findByAuthorIdOrderByCreatedDesc(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);
}
