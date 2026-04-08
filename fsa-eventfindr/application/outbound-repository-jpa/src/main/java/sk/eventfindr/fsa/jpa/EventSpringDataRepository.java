package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.Event;

import java.util.Collection;

interface EventSpringDataRepository extends JpaRepository<Event, Long> {

    Collection<Event> findByLocationContainingIgnoreCase(String location);

    Collection<Event> findByNameContainingIgnoreCase(String name);
}
