package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;

import java.util.Collection;

public interface EventFacade {

    Collection<Event> readAll();

    Event getById(Long id);

    void create(Event event);

    void attend(Long eventId, Long userId, String status);

    void unattend(Long eventId, Long userId);

    AttendanceStatus getAttendanceStatus(Long eventId, Long userId);

    Collection<EventAttendance> getAttendancesByUser(Long userId);
}
