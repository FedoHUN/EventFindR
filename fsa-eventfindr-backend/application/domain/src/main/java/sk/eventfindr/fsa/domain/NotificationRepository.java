package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface NotificationRepository {

    void save(Notification notification);

    Optional<Notification> findById(Long id);

    Collection<Notification> findByUserId(Long userId);

    Collection<Notification> findUnreadByUserId(Long userId);

    int countUnreadByUserId(Long userId);

    boolean existsByUserIdAndEventIdAndType(Long userId, Long eventId, NotificationType type);

    void markAsRead(Long id);

    void markAllAsReadByUserId(Long userId);
}
