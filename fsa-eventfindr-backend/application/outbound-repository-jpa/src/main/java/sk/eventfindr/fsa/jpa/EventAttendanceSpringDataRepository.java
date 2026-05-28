package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sk.eventfindr.fsa.domain.EventAttendance;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface EventAttendanceSpringDataRepository extends JpaRepository<EventAttendance, Long> {

    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);

    Collection<EventAttendance> findByUserId(Long userId);

    Collection<EventAttendance> findByEventId(Long eventId);

    int countByEventIdAndStatus(Long eventId, sk.eventfindr.fsa.domain.AttendanceStatus status);

    @Query("""
            select a.event.id, a.status, count(a)
            from EventAttendance a
            where a.event.id in :eventIds
            group by a.event.id, a.status
            """)
    List<Object[]> countByEventIdsGrouped(Collection<Long> eventIds);
}
