package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface EventAttendanceRepository {

    void create(EventAttendance attendance);

    Optional<EventAttendance> findByEventAndUser(Long eventId, Long userId);

    Collection<EventAttendance> findByUser(Long userId);

    void delete(EventAttendance attendance);
}
