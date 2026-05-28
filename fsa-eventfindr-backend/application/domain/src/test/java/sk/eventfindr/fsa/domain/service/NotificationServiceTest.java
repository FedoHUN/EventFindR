package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.NotificationRepository;
import sk.eventfindr.fsa.domain.NotificationType;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserFollowRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventAttendanceRepository attendanceRepository;

    @Mock
    private UserFollowRepository followRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void markAsReadRejectsWrongUser() {
        Notification notification = new Notification();
        notification.setId(10L);
        notification.setUserId(1L);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.markAsRead(10L, 2L));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        verify(notificationRepository, never()).markAsRead(10L);
    }

    @Test
    void createEventRemindersSkipsExistingReminder() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-27T10:00:00Z"), ZoneOffset.UTC);
        NotificationService reminderService = new NotificationService(
                notificationRepository,
                eventRepository,
                attendanceRepository,
                followRepository,
                fixedClock,
                DomainLogger.noop());

        Event event = new Event();
        event.setId(20L);
        event.setName("Architecture Meetup");
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(Date.from(Instant.parse("2026-05-28T18:00:00Z")));

        User user = new User();
        user.setId(5L);

        EventAttendance attendance = new EventAttendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.ATTENDING);

        when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of(event));
        when(attendanceRepository.findByEvent(20L)).thenReturn(List.of(attendance));
        when(notificationRepository.existsByUserIdAndEventIdAndType(5L, 20L, NotificationType.EVENT_REMINDER))
                .thenReturn(true);

        reminderService.createEventReminders();

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any(Notification.class));
    }
}
