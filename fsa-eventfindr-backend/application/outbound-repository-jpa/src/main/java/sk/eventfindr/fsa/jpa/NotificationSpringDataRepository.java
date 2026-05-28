package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import sk.eventfindr.fsa.domain.Notification;

import java.util.Collection;

interface NotificationSpringDataRepository extends JpaRepository<Notification, Long> {

    Collection<Notification> findByUserIdOrderByCreatedDesc(Long userId);

    Collection<Notification> findByUserIdAndReadFalseOrderByCreatedDesc(Long userId);

    int countByUserIdAndReadFalse(Long userId);

    boolean existsByUserIdAndEventIdAndType(Long userId, Long eventId, sk.eventfindr.fsa.domain.NotificationType type);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    void markAllAsReadByUserId(Long userId);
}
