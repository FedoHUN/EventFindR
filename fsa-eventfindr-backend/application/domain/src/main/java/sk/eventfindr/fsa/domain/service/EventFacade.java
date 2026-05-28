package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Event;

import java.util.Collection;

public interface EventFacade {

    Collection<Event> readAll();

    Collection<Event> readAllPublished();

    Event getById(Long id);

    Long create(Event event);

    void update(Event event, Long userId);

    void cancelEvent(Long eventId, Long userId);

    void restoreEvent(Long eventId, Long userId);

    void deleteEvent(Long eventId, Long userId);

    void publishEvent(Long eventId, Long userId);

    void toggleFeatured(Long eventId, Long userId);
}
