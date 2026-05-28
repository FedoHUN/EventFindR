package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sk.eventfindr.fsa.domain.EventComment;

import java.util.Collection;
import java.util.List;

interface EventCommentSpringDataRepository extends JpaRepository<EventComment, Long> {

    Collection<EventComment> findByEventIdOrderByCreatedDesc(Long eventId);

    int countByEventId(Long eventId);

    @Query("SELECT AVG(c.rating) FROM EventComment c WHERE c.eventId = :eventId AND c.rating IS NOT NULL")
    Double getAverageRatingByEventId(Long eventId);

    @Query("SELECT COUNT(c) FROM EventComment c WHERE c.eventId = :eventId AND c.rating IS NOT NULL")
    int countRatingsByEventId(Long eventId);

    @Query("""
            select c.eventId, count(c)
            from EventComment c
            where c.eventId in :eventIds
            group by c.eventId
            """)
    List<Object[]> countByEventIdsGrouped(Collection<Long> eventIds);

    @Query("""
            select c.eventId, avg(c.rating)
            from EventComment c
            where c.eventId in :eventIds and c.rating is not null
            group by c.eventId
            """)
    List<Object[]> averageRatingsByEventIds(Collection<Long> eventIds);

    @Query("""
            select c.eventId, count(c)
            from EventComment c
            where c.eventId in :eventIds and c.rating is not null
            group by c.eventId
            """)
    List<Object[]> ratingCountsByEventIds(Collection<Long> eventIds);
}
