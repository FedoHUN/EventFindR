package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.EventCommentRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaEventCommentRepositoryAdapter implements EventCommentRepository {

    private final EventCommentSpringDataRepository repository;

    public JpaEventCommentRepositoryAdapter(EventCommentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(EventComment comment) {
        repository.save(comment);
    }

    @Override
    public Optional<EventComment> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Collection<EventComment> findByEventId(Long eventId) {
        return repository.findByEventIdOrderByCreatedDesc(eventId);
    }

    @Override
    public int countByEventId(Long eventId) {
        return repository.countByEventId(eventId);
    }

    @Override
    public Map<Long, Integer> countByEventIds(Collection<Long> eventIds) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        for (Object[] row : repository.countByEventIdsGrouped(eventIds)) {
            counts.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return counts;
    }

    @Override
    public Double getAverageRating(Long eventId) {
        return repository.getAverageRatingByEventId(eventId);
    }

    @Override
    public Map<Long, Double> getAverageRatingByEventIds(Collection<Long> eventIds) {
        Map<Long, Double> ratings = new LinkedHashMap<>();
        for (Object[] row : repository.averageRatingsByEventIds(eventIds)) {
            ratings.put((Long) row[0], row[1] == null ? null : ((Double) row[1]));
        }
        return ratings;
    }

    @Override
    public int countRatings(Long eventId) {
        return repository.countRatingsByEventId(eventId);
    }

    @Override
    public Map<Long, Integer> countRatingsByEventIds(Collection<Long> eventIds) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        for (Object[] row : repository.ratingCountsByEventIds(eventIds)) {
            counts.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return counts;
    }

    @Override
    public void delete(EventComment comment) {
        repository.delete(comment);
    }
}
