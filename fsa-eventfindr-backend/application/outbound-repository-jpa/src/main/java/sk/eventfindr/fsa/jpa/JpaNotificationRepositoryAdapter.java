package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.NotificationRepository;
import sk.eventfindr.fsa.domain.NotificationType;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationSpringDataRepository repository;

    public JpaNotificationRepositoryAdapter(NotificationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Notification notification) {
        repository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Collection<Notification> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedDesc(userId);
    }

    @Override
    public Collection<Notification> findUnreadByUserId(Long userId) {
        return repository.findByUserIdAndReadFalseOrderByCreatedDesc(userId);
    }

    @Override
    public int countUnreadByUserId(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public boolean existsByUserIdAndEventIdAndType(Long userId, Long eventId, NotificationType type) {
        return repository.existsByUserIdAndEventIdAndType(userId, eventId, type);
    }

    @Override
    public void markAsRead(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setRead(true);
            repository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsReadByUserId(Long userId) {
        repository.markAllAsReadByUserId(userId);
    }
}
