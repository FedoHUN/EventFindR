package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;

import java.time.Clock;
import java.util.Collection;
import java.util.Map;

public class EventAttendanceService implements EventAttendanceFacade {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final Clock clock;
    private final DomainLogger log;

    public EventAttendanceService(EventRepository eventRepository,
                                  UserRepository userRepository,
                                  EventAttendanceRepository eventAttendanceRepository) {
        this(eventRepository, userRepository, eventAttendanceRepository, Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public EventAttendanceService(EventRepository eventRepository,
                                  UserRepository userRepository,
                                  EventAttendanceRepository eventAttendanceRepository,
                                  Clock clock,
                                  DomainLogger log) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public void attend(Long eventId, Long userId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event with ID " + eventId + " was not found"));

        User user = userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));
        AttendanceStatus attendanceStatus = parseAttendanceStatus(status);

        if (eventAttendanceRepository.findByEventAndUser(eventId, userId).isPresent()) {
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "User already has attendance registered for this event");
        }

        if (attendanceStatus == AttendanceStatus.ATTENDING && event.getCapacity() != null) {
            int currentAttending = eventAttendanceRepository.countByEventIdAndStatus(eventId, AttendanceStatus.ATTENDING);
            if (currentAttending >= event.getCapacity()) {
                throw new EventfindrException(
                        EventfindrException.Type.VALIDATION,
                        "Event capacity has been reached");
            }
        }

        EventAttendance attendance = new EventAttendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(attendanceStatus);
        attendance.prepareForCreation(clock.instant());
        eventAttendanceRepository.create(attendance);
        log.info("User {} registered {} for event {}", userId, attendanceStatus, eventId);
    }

    @Override
    public void unattend(Long eventId, Long userId) {
        EventAttendance attendance = eventAttendanceRepository.findByEventAndUser(eventId, userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Attendance was not found"));
        eventAttendanceRepository.delete(attendance);
        log.info("User {} removed attendance from event {}", userId, eventId);
    }

    @Override
    public AttendanceStatus getAttendanceStatus(Long eventId, Long userId) {
        return eventAttendanceRepository.findByEventAndUser(eventId, userId)
                .map(EventAttendance::getStatus)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Attendance was not found"));
    }

    @Override
    public Collection<EventAttendance> getAttendancesByUser(Long userId) {
        return eventAttendanceRepository.findByUser(userId);
    }

    @Override
    public Map<String, Integer> getAttendanceCounts(Long eventId) {
        return Map.of(
                "attending", eventAttendanceRepository.countByEventIdAndStatus(eventId, AttendanceStatus.ATTENDING),
                "watching", eventAttendanceRepository.countByEventIdAndStatus(eventId, AttendanceStatus.WATCHING)
        );
    }

    private AttendanceStatus parseAttendanceStatus(String status) {
        try {
            return AttendanceStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Invalid attendance status: " + status);
        }
    }
}
