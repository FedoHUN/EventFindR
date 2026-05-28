package sk.eventfindr.fsa.domain;

import java.time.Instant;
import java.util.Date;

public class EventAttendance {

    private Long id;
    private Event event;
    private User user;
    private AttendanceStatus status;
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void prepareForCreation() {
        prepareForCreation(Instant.now());
    }

    public void prepareForCreation(Instant now) {
        if (event == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event is required");
        }
        if (user == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "User is required");
        }
        if (status == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Attendance status is required");
        }
        if (created == null) {
            created = Date.from(now);
        }
    }
}
