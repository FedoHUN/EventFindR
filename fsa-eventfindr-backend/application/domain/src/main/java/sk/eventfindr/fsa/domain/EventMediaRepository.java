package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface EventMediaRepository {

    void save(EventMedia media);

    Optional<EventMedia> findById(Long id);

    Collection<EventMedia> findByEventId(Long eventId);

    Map<Long, EventMedia> findFirstImageByEventIds(Collection<Long> eventIds);

    void delete(EventMedia media);

    void deleteAllByEventId(Long eventId);

    int countByEventIdAndMediaType(Long eventId, MediaType mediaType);
}
