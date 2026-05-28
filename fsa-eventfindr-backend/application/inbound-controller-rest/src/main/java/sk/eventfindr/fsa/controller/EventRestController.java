package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.EventAttendanceFacade;
import sk.eventfindr.fsa.domain.service.EventDiscoveryFacade;
import sk.eventfindr.fsa.domain.service.EventFacade;
import sk.eventfindr.fsa.mapper.EventMapper;
import sk.eventfindr.fsa.rest.api.EventsApi;
import sk.eventfindr.fsa.rest.dto.AttendEventRequestDto;
import sk.eventfindr.fsa.rest.dto.CreateEventRequestDto;
import sk.eventfindr.fsa.rest.dto.EventDto;
import sk.eventfindr.fsa.rest.dto.EventsResponseDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class EventRestController implements EventsApi {

    private final EventFacade eventFacade;
    private final EventAttendanceFacade eventAttendanceFacade;
    private final EventDiscoveryFacade eventDiscoveryFacade;
    private final EventMapper eventMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public EventRestController(EventFacade eventFacade,
                               EventAttendanceFacade eventAttendanceFacade,
                               EventDiscoveryFacade eventDiscoveryFacade,
                               EventMapper eventMapper,
                               CurrentUserDetailService currentUserDetailService) {
        this.eventFacade = eventFacade;
        this.eventAttendanceFacade = eventAttendanceFacade;
        this.eventDiscoveryFacade = eventDiscoveryFacade;
        this.eventMapper = eventMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @Override
    public ResponseEntity<EventsResponseDto> getAllEvents() {
        Collection<Event> events = eventFacade.readAllPublished();
        EventsResponseDto response = new EventsResponseDto();
        response.setEvents(eventMapper.toDtoList(events));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EventDto> getEventById(Long id) {
        Event event = eventFacade.getById(id);
        return ResponseEntity.ok(eventMapper.toDto(event));
    }

    @Override
    public ResponseEntity<Void> createEvent(CreateEventRequestDto request) {
        User organizer = currentUserDetailService.getFullCurrentUser();
        Event event = eventMapper.toEntity(request);
        event.setOrganizer(organizer);
        Long eventId = eventFacade.create(event);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(eventId)
                .toUri();
        return ResponseEntity.created(location)
                .header("X-Event-Id", String.valueOf(eventId))
                .build();
    }

    @Override
    public ResponseEntity<Void> attendEvent(Long id, AttendEventRequestDto request) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventAttendanceFacade.attend(id, user.getId(), request.getStatus().name());
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/events/{id}/attend")
    public ResponseEntity<Map<String, String>> getMyAttendance(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        String status = eventAttendanceFacade.getAttendanceStatus(id, user.getId()).name();
        return ResponseEntity.ok(Map.of("status", status));
    }

    @DeleteMapping("/events/{id}/attend")
    public ResponseEntity<Void> unattendEvent(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventAttendanceFacade.unattend(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.cancelEvent(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{id}/restore")
    public ResponseEntity<Void> restoreEvent(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.restoreEvent(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.deleteEvent(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/my-attendances")
    public ResponseEntity<List<EventDto>> getMyAttendances() {
        User user = currentUserDetailService.getFullCurrentUser();
        Collection<sk.eventfindr.fsa.domain.EventAttendance> attendances = eventAttendanceFacade.getAttendancesByUser(user.getId());
        List<EventDto> dtos = attendances.stream()
                .map(a -> {
                    EventDto dto = eventMapper.toDto(a.getEvent());
                    // Store the attendance status in the DTO so frontend knows which type
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/events/my-performances")
    public ResponseEntity<List<EventDto>> getMyPerformances() {
        User user = currentUserDetailService.getFullCurrentUser();
        Collection<Event> events = eventDiscoveryFacade.findByArtistUserId(user.getId());
        List<EventDto> dtos = events.stream()
                .map(eventMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/events/my-drafts")
    public ResponseEntity<List<EventDto>> getMyDrafts() {
        User user = currentUserDetailService.getFullCurrentUser();
        List<EventDto> dtos = eventDiscoveryFacade.findDraftsByOrganizer(user.getId()).stream()
                .map(eventMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable("id") Long id,
                                                @RequestBody CreateEventRequestDto request) {
        User user = currentUserDetailService.getFullCurrentUser();
        Event event = eventMapper.toEntity(request);
        event.setId(id);
        eventFacade.update(event, user.getId());
        Event updated = eventFacade.getById(id);
        return ResponseEntity.ok(eventMapper.toDto(updated));
    }

    @PostMapping("/events/{id}/publish")
    public ResponseEntity<Void> publishEvent(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.publishEvent(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{id}/toggle-featured")
    public ResponseEntity<Void> toggleFeatured(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.toggleFeatured(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/trending")
    public ResponseEntity<List<EventDto>> getTrendingEvents() {
        Collection<Event> trending = eventDiscoveryFacade.findTrending(6);
        return ResponseEntity.ok(trending.stream().map(eventMapper::toDto).toList());
    }

    @GetMapping("/events/{id}/similar")
    public ResponseEntity<List<EventDto>> getSimilarEvents(@PathVariable("id") Long id) {
        Collection<Event> similar = eventDiscoveryFacade.findSimilar(id);
        return ResponseEntity.ok(similar.stream().map(eventMapper::toDto).toList());
    }

    @GetMapping("/events/{id}/attendance-counts")
    public ResponseEntity<Map<String, Integer>> getAttendanceCounts(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventAttendanceFacade.getAttendanceCounts(id));
    }

}
