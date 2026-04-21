package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;

import java.util.Collection;

public class EventService implements EventFacade {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventAttendanceRepository eventAttendanceRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventAttendanceRepository eventAttendanceRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
    }

    @Override
    public Collection<Event> readAll() {
        return eventRepository.readAll();
    }

    @Override
    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event s ID " + id + " nebol nájdený"));
    }

    @Override
    public void create(Event event) {
        if (event == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event nesmie byť null");
        }
        event.prepareForCreation();
        eventRepository.create(event);
    }

    @Override
    public void attend(Long eventId, Long userId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event s ID " + eventId + " nebol nájdený"));

        User user = userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Používateľ s ID " + userId + " nebol nájdený"));

        AttendanceStatus attendanceStatus;
        try {
            attendanceStatus = AttendanceStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Neplatný status účasti: " + status);
        }

        var existing = eventAttendanceRepository.findByEventAndUser(eventId, userId);
        if (existing.isPresent()) {
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "Používateľ už má zaregistrovanú účasť na tomto evente");
        }

        EventAttendance attendance = new EventAttendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(attendanceStatus);
        attendance.prepareForCreation();
        eventAttendanceRepository.create(attendance);
    }

    @Override
    public Collection<EventAttendance> getAttendancesByUser(Long userId) {
        return eventAttendanceRepository.findByUser(userId);
    }
}
