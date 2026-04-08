package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface EventRepository {

    Collection<Event> readAll();

    Optional<Event> findById(Long id);

    Collection<Event> findByLocation(String location);

    Collection<Event> findByName(String name);

    void create(Event event);
}
