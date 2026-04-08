package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventAttendanceTest {

    @Test
    void prepareForCreationSetsCreatedTimestamp() {
        EventAttendance attendance = validAttendance();

        attendance.prepareForCreation();

        assertNotNull(attendance.getCreated());
    }

    @Test
    void prepareForCreationFailsWhenEventMissing() {
        EventAttendance attendance = validAttendance();
        attendance.setEvent(null);

        EventfindrException ex = assertThrows(EventfindrException.class, attendance::prepareForCreation);
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void prepareForCreationFailsWhenUserMissing() {
        EventAttendance attendance = validAttendance();
        attendance.setUser(null);

        EventfindrException ex = assertThrows(EventfindrException.class, attendance::prepareForCreation);
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void prepareForCreationFailsWhenStatusMissing() {
        EventAttendance attendance = validAttendance();
        attendance.setStatus(null);

        EventfindrException ex = assertThrows(EventfindrException.class, attendance::prepareForCreation);
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    private EventAttendance validAttendance() {
        Event event = new Event();
        event.setId(1L);

        User user = new User();
        user.setId(1L);

        EventAttendance attendance = new EventAttendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.ATTENDING);
        return attendance;
    }
}
