package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public Collection<EventAttendance> findByEvent(Long eventId) {
        return repository.findByEventId(eventId);
    }

    @Override
    public int countByEventIdAndStatus(Long eventId, AttendanceStatus status) {
        return repository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public Map<Long, Map<AttendanceStatus, Integer>> countsByEventIds(Collection<Long> eventIds) {
        Map<Long, Map<AttendanceStatus, Integer>> counts = new LinkedHashMap<>();
        for (Object[] row : repository.countByEventIdsGrouped(eventIds)) {
            Long eventId = (Long) row[0];
            AttendanceStatus status = (AttendanceStatus) row[1];
            int count = ((Long) row[2]).intValue();
            counts.computeIfAbsent(eventId, ignored -> new LinkedHashMap<>()).put(status, count);
        }
        return counts;
    }

    @Override
    public void delete(EventAttendance attendance) {
        repository.delete(attendance);
    }
}
