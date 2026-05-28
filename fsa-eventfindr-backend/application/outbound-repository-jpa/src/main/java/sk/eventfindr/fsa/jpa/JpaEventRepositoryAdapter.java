package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;

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
    public Collection<Event> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.List.of();
        }
        return eventSpringDataRepository.findByIdIn(ids);
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

    @Override
    public Collection<Event> findByGenre(String genre) {
        return eventSpringDataRepository.findByGenreIgnoreCase(genre);
    }

    @Override
    public Collection<Event> findByStatus(EventStatus status) {
        return eventSpringDataRepository.findByStatus(status);
    }

    @Override
    public Collection<Event> findByOrganizerIdAndStatus(Long organizerId, EventStatus status) {
        return eventSpringDataRepository.findByOrganizer_IdAndStatus(organizerId, status);
    }

    @Override
    public void update(Event event) {
        eventSpringDataRepository.save(event);
    }

    @Override
    public void delete(Long id) {
        eventSpringDataRepository.deleteById(id);
    }
}
