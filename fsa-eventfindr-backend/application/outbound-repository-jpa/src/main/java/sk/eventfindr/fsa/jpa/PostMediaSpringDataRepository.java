package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.MediaType;
import sk.eventfindr.fsa.domain.PostMedia;

import java.util.Collection;

interface PostMediaSpringDataRepository extends JpaRepository<PostMedia, Long> {

    Collection<PostMedia> findByPostIdOrderBySortOrder(Long postId);

    int countByPostIdAndMediaType(Long postId, MediaType mediaType);

    void deleteAllByPostId(Long postId);
}
