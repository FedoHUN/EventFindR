package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaEventRepositoryAdapter implements EventRepository {

    private final EventSpringDataRepository eventSpringDataRepository;

    public JpaEventRepositoryAdapter(EventSpringDataRepository eventSpringDataRepository) {
        this.eventSpringDataRepository = eventSpringDataRepository;
    }

    @Override
    public Collection<Event> readAll() {
        return eventSpringDataRepository.findAll();
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventSpringDataRepository.findById(id);
    }

    @Override
    public Collection<Event> findByLocation(String location) {
        return eventSpringDataRepository.findByLocationContainingIgnoreCase(location);
    }

    @Override
    public Collection<Event> findByName(String name) {
        return eventSpringDataRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public void create(Event event) {
        eventSpringDataRepository.save(event);
    }
}
