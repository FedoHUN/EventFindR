package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.NotificationRepository;
import sk.eventfindr.fsa.domain.NotificationType;
import sk.eventfindr.fsa.domain.UserFollow;
import sk.eventfindr.fsa.domain.UserFollowRepository;

import java.time.Clock;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class NotificationService implements NotificationFacade {

    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final EventAttendanceRepository attendanceRepository;
    private final UserFollowRepository followRepository;
    private final Clock clock;
    private final DomainLogger log;

    public NotificationService(NotificationRepository notificationRepository,
                               EventRepository eventRepository,
                               EventAttendanceRepository attendanceRepository,
                               UserFollowRepository followRepository) {
        this(notificationRepository, eventRepository, attendanceRepository, followRepository,
                Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public NotificationService(NotificationRepository notificationRepository,
                               EventRepository eventRepository,
                               EventAttendanceRepository attendanceRepository,
                               UserFollowRepository followRepository,
                               Clock clock,
                               DomainLogger log) {
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.followRepository = followRepository;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public Collection<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Notification was not found"));
        if (!notification.getUserId().equals(userId)) {
            log.warn("Rejected notification read for notification {} by user {}", notificationId, userId);
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Cannot mark another user's notification as read");
        }
        notificationRepository.markAsRead(notificationId);
        log.info("Marked notification {} as read for user {}", notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    @Override
    public void createEventReminders() {
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.setTime(Date.from(clock.instant()));
        tomorrowCalendar.add(Calendar.DAY_OF_MONTH, 1);

        Collection<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED);
        for (Event event : events) {
            if (event.isCanceled() || event.getEventDate() == null || !isSameDay(event.getEventDate(), tomorrowCalendar)) {
                continue;
            }

            Collection<EventAttendance> attendances = attendanceRepository.findByEvent(event.getId());
            for (EventAttendance attendance : attendances) {
                if (attendance.getStatus() != AttendanceStatus.ATTENDING) {
                    continue;
                }
                if (notificationRepository.existsByUserIdAndEventIdAndType(
                        attendance.getUser().getId(),
                        event.getId(),
                        NotificationType.EVENT_REMINDER)) {
                    continue;
                }
                createNotification(
                        attendance.getUser().getId(),
                        event.getId(),
                        NotificationType.EVENT_REMINDER,
                        "Reminder: \"" + event.getName() + "\" is tomorrow!"
                );
            }
        }
    }

    @Override
    public void notifyFollowersOfNewEvent(Long organizerId, Long eventId, String eventName) {
        Collection<UserFollow> followers = followRepository.findByFollowed(organizerId);
        for (UserFollow follow : followers) {
            createNotification(
                    follow.getFollower().getId(),
                    eventId,
                    NotificationType.NEW_EVENT,
                    "New event from an organizer you follow: \"" + eventName + "\""
            );
        }
        log.info("Created new-event notifications for organizer {} and event {}", organizerId, eventId);
    }

    @Override
    public void notifyAttendeesOfCancellation(Long eventId, String eventName) {
        Collection<EventAttendance> attendances = attendanceRepository.findByEvent(eventId);
        for (EventAttendance attendance : attendances) {
            createNotification(
                    attendance.getUser().getId(),
                    eventId,
                    NotificationType.EVENT_CANCELED,
                    "\"" + eventName + "\" has been canceled"
            );
        }
        log.info("Created cancellation notifications for event {}", eventId);
    }

    @Override
    public void notifyAttendeesOfUpdate(Long eventId, String eventName) {
        Collection<EventAttendance> attendances = attendanceRepository.findByEvent(eventId);
        for (EventAttendance attendance : attendances) {
            createNotification(
                    attendance.getUser().getId(),
                    eventId,
                    NotificationType.EVENT_UPDATED,
                    "\"" + eventName + "\" has been updated"
            );
        }
        log.info("Created update notifications for event {}", eventId);
    }

    @Override
    public void notifyEventOrganizerOfComment(Long eventId, Long organizerId, String commenterName) {
        createNotification(
                organizerId,
                eventId,
                NotificationType.NEW_COMMENT,
                commenterName + " commented on your event"
        );
        log.info("Created organizer comment notification for event {}", eventId);
    }

    @Override
    public void notifyUserOfNewFollower(Long followedId, String followerName) {
        createNotification(
                followedId,
                null,
                NotificationType.NEW_FOLLOWER,
                followerName + " started following you"
        );
        log.info("Created follower notification for user {}", followedId);
    }

    private boolean isSameDay(Date eventDate, Calendar targetDay) {
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTime(eventDate);
        return eventCalendar.get(Calendar.YEAR) == targetDay.get(Calendar.YEAR)
                && eventCalendar.get(Calendar.DAY_OF_YEAR) == targetDay.get(Calendar.DAY_OF_YEAR);
    }

    private void createNotification(Long userId, Long eventId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEventId(eventId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreated(Date.from(clock.instant()));
        notificationRepository.save(notification);
    }
}
