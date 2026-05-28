package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventMediaRepository;
import sk.eventfindr.fsa.domain.MediaType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaEventMediaRepositoryAdapter implements EventMediaRepository {

    private final EventMediaSpringDataRepository repository;

    public JpaEventMediaRepositoryAdapter(EventMediaSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(EventMedia media) {
        repository.save(media);
    }

    @Override
    public Optional<EventMedia> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Collection<EventMedia> findByEventId(Long eventId) {
        return repository.findByEventIdOrderBySortOrder(eventId);
    }

    @Override
    public Map<Long, EventMedia> findFirstImageByEventIds(Collection<Long> eventIds) {
        Map<Long, EventMedia> firstImages = new LinkedHashMap<>();
        for (EventMedia media : repository.findByEventIdInAndMediaTypeOrderByEventIdAscSortOrderAsc(eventIds, MediaType.IMAGE)) {
            firstImages.putIfAbsent(media.getEventId(), media);
        }
        return firstImages;
    }

    @Override
    public void delete(EventMedia media) {
        repository.delete(media);
    }

    @Override
    @Transactional
    public void deleteAllByEventId(Long eventId) {
        repository.deleteAllByEventId(eventId);
    }

    @Override
    public int countByEventIdAndMediaType(Long eventId, MediaType mediaType) {
        return repository.countByEventIdAndMediaType(eventId, mediaType);
    }
}
