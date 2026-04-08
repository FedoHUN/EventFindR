package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.EventAttendance;

import java.util.Collection;
import java.util.Optional;

interface EventAttendanceSpringDataRepository extends JpaRepository<EventAttendance, Long> {

    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);

    Collection<EventAttendance> findByUserId(Long userId);
}
