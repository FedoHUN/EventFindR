package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventAttendanceRepository eventAttendanceRepository;

    @InjectMocks
    private EventService service;

    @Test
    void createPreparesEventAndPersistsIt() {
        Event event = validEvent();

        service.create(event);

        assertEquals("Pohoda Festival", event.getName());
        assertNotNull(event.getCreated());
        verify(eventRepository).create(event);
    }

    @Test
    void createFailsWhenEventIsNull() {
        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.create(null));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        verify(eventRepository, never()).create(any());
    }

    @Test
    void getByIdReturnsEventWhenFound() {
        Event event = validEvent();
        event.setId(1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = service.getById(1L);

        assertEquals(1L, result.getId());
        verify(eventRepository).findById(1L);
    }

    @Test
    void getByIdThrowsNotFoundWhenMissing() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.getById(999L));

        assertEquals(EventfindrException.Type.NOT_FOUND, ex.getType());
    }

    @Test
    void readAllReturnsAllEvents() {
        List<Event> events = List.of(validEvent(), validEvent());
        when(eventRepository.readAll()).thenReturn(events);

        var result = service.readAll();

        assertEquals(2, result.size());
        verify(eventRepository).readAll();
    }

    @Test
    void attendCreatesAttendanceForValidEventAndUser() {
        Event event = validEvent();
        event.setId(1L);
        User user = organizer();
        user.setId(2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(2L)).thenReturn(Optional.of(user));
        when(eventAttendanceRepository.findByEventAndUser(1L, 2L)).thenReturn(Optional.empty());

        service.attend(1L, 2L, "ATTENDING");

        verify(eventAttendanceRepository).create(any());
    }

    @Test
    void attendThrowsNotFoundWhenEventMissing() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.attend(999L, 1L, "ATTENDING"));

        assertEquals(EventfindrException.Type.NOT_FOUND, ex.getType());
    }

    @Test
    void attendThrowsNotFoundWhenUserMissing() {
        Event event = validEvent();
        event.setId(1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(999L)).thenReturn(Optional.empty());

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.attend(1L, 999L, "ATTENDING"));

        assertEquals(EventfindrException.Type.NOT_FOUND, ex.getType());
    }

    @Test
    void attendThrowsConflictWhenAlreadyAttending() {
        Event event = validEvent();
        event.setId(1L);
        User user = organizer();
        user.setId(2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(2L)).thenReturn(Optional.of(user));
        when(eventAttendanceRepository.findByEventAndUser(1L, 2L)).thenReturn(Optional.of(new sk.eventfindr.fsa.domain.EventAttendance()));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.attend(1L, 2L, "ATTENDING"));

        assertEquals(EventfindrException.Type.CONFLICT, ex.getType());
        verify(eventAttendanceRepository, never()).create(any());
    }

    @Test
    void attendThrowsValidationForInvalidStatus() {
        Event event = validEvent();
        event.setId(1L);
        User user = organizer();
        user.setId(2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(2L)).thenReturn(Optional.of(user));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.attend(1L, 2L, "INVALID_STATUS"));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    private Event validEvent() {
        Event event = new Event();
        event.setName("  Pohoda Festival  ");
        event.setLocation("Trenčín");
        event.setEventDate(new Date());
        event.setPrice(BigDecimal.valueOf(89.99));
        event.setOrganizer(organizer());
        return event;
    }

    private User organizer() {
        User user = new User();
        user.setId(1L);
        user.setName("Organizer");
        user.setEmail("org@eventfindr.sk");
        user.setRola(UserRole.ORGANIZER);
        return user;
    }
}
