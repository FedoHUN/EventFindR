package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventArtistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JpaEventArtistRepositoryAdapter implements EventArtistRepository {

    private final EventArtistSpringDataRepository repository;

    public JpaEventArtistRepositoryAdapter(EventArtistSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Collection<EventArtist> findByEventId(Long eventId) {
        return repository.findByEventIdOrderBySortOrder(eventId);
    }

    @Override
    public Map<Long, List<EventArtist>> findByEventIds(Collection<Long> eventIds) {
        Map<Long, List<EventArtist>> grouped = new LinkedHashMap<>();
        for (EventArtist artist : repository.findByEventIdInOrderByEventIdAscSortOrderAsc(eventIds)) {
            grouped.computeIfAbsent(artist.getEventId(), ignored -> new java.util.ArrayList<>()).add(artist);
        }
        return grouped;
    }

    @Override
    public Collection<EventArtist> findByArtistUserId(Long artistUserId) {
        return repository.findByArtistUserId(artistUserId);
    }

    @Override
    public void save(EventArtist eventArtist) {
        repository.save(eventArtist);
    }

    @Override
    @Transactional
    public void deleteByEventId(Long eventId) {
        repository.deleteByEventId(eventId);
    }
}
