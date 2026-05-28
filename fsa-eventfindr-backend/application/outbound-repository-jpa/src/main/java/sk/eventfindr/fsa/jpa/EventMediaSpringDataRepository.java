package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.MediaType;

import java.util.Collection;

interface EventMediaSpringDataRepository extends JpaRepository<EventMedia, Long> {

    Collection<EventMedia> findByEventIdOrderBySortOrder(Long eventId);

    Collection<EventMedia> findByEventIdInAndMediaTypeOrderByEventIdAscSortOrderAsc(Collection<Long> eventIds, MediaType mediaType);

    int countByEventIdAndMediaType(Long eventId, MediaType mediaType);

    void deleteAllByEventId(Long eventId);
}
