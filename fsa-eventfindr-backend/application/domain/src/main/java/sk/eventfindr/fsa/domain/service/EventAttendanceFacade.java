package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.EventAttendance;

import java.util.Collection;
import java.util.Map;

public interface EventAttendanceFacade {

    void attend(Long eventId, Long userId, String status);

    void unattend(Long eventId, Long userId);

    AttendanceStatus getAttendanceStatus(Long eventId, Long userId);

    Collection<EventAttendance> getAttendancesByUser(Long userId);

    Map<String, Integer> getAttendanceCounts(Long eventId);
}
