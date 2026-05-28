package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface EventCommentRepository {

    void save(EventComment comment);

    Optional<EventComment> findById(Long id);

    Collection<EventComment> findByEventId(Long eventId);

    int countByEventId(Long eventId);

    Map<Long, Integer> countByEventIds(Collection<Long> eventIds);

    Double getAverageRating(Long eventId);

    Map<Long, Double> getAverageRatingByEventIds(Collection<Long> eventIds);

    int countRatings(Long eventId);

    Map<Long, Integer> countRatingsByEventIds(Collection<Long> eventIds);

    void delete(EventComment comment);
}
