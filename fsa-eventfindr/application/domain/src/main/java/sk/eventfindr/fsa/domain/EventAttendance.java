package sk.eventfindr.fsa.domain;

import java.util.Date;

public class EventAttendance {

    private Long id;
    private Event event;
    private User user;
    private AttendanceStatus status;
    private Date created;

    public EventAttendance() {
    }

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
        if (event == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event je povinný údaj");
        }
        if (user == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Používateľ je povinný údaj");
        }
        if (status == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Status účasti je povinný údaj");
        }
        if (created == null) {
            created = new Date();
        }
    }
}
