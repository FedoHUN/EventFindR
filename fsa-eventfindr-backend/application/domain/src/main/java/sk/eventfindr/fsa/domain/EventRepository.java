package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface EventRepository {

    Collection<Event> readAll();

    Optional<Event> findById(Long id);

    Collection<Event> findByIds(Collection<Long> ids);

    Collection<Event> findByLocation(String location);

    Collection<Event> findByName(String name);

    void create(Event event);

    Collection<Event> findByGenre(String genre);

    Collection<Event> findByStatus(EventStatus status);

    Collection<Event> findByOrganizerIdAndStatus(Long organizerId, EventStatus status);

    void update(Event event);

    void delete(Long id);
}
