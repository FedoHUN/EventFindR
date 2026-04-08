package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaEventAttendanceRepositoryAdapter implements EventAttendanceRepository {

    private final EventAttendanceSpringDataRepository repository;

    public JpaEventAttendanceRepositoryAdapter(EventAttendanceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(EventAttendance attendance) {
        repository.save(attendance);
    }

    @Override
    public Optional<EventAttendance> findByEventAndUser(Long eventId, Long userId) {
        return repository.findByEventIdAndUserId(eventId, userId);
    }

    @Override
    public Collection<EventAttendance> findByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void delete(EventAttendance attendance) {
        repository.delete(attendance);
    }
}
