package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;

import java.util.Collection;

public interface EventFacade {

    Collection<Event> readAll();

    Event getById(Long id);

    void create(Event event);

    void attend(Long eventId, Long userId, String status);

    Collection<EventAttendance> getAttendancesByUser(Long userId);
}
