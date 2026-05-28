package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.EventArtist;

import java.util.Collection;

interface EventArtistSpringDataRepository extends JpaRepository<EventArtist, Long> {

    Collection<EventArtist> findByEventIdOrderBySortOrder(Long eventId);

    Collection<EventArtist> findByEventIdInOrderByEventIdAscSortOrderAsc(Collection<Long> eventIds);

    Collection<EventArtist> findByArtistUserId(Long artistUserId);

    void deleteByEventId(Long eventId);
}
