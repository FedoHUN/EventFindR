package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface EventAttendanceRepository {

    void create(EventAttendance attendance);

    Optional<EventAttendance> findByEventAndUser(Long eventId, Long userId);

    Collection<EventAttendance> findByUser(Long userId);

    Collection<EventAttendance> findByEvent(Long eventId);

    int countByEventIdAndStatus(Long eventId, AttendanceStatus status);

    Map<Long, Map<AttendanceStatus, Integer>> countsByEventIds(Collection<Long> eventIds);

    void delete(EventAttendance attendance);
}
