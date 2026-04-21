package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.EventFacade;
import sk.eventfindr.fsa.mapper.EventMapper;
import sk.eventfindr.fsa.rest.api.EventsApi;
import sk.eventfindr.fsa.rest.dto.AttendEventRequestDto;
import sk.eventfindr.fsa.rest.dto.CreateEventRequestDto;
import sk.eventfindr.fsa.rest.dto.EventDto;
import sk.eventfindr.fsa.rest.dto.EventsResponseDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;

@RestController
public class EventRestController implements EventsApi {

    private final EventFacade eventFacade;
    private final EventMapper eventMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public EventRestController(EventFacade eventFacade,
                               EventMapper eventMapper,
                               CurrentUserDetailService currentUserDetailService) {
        this.eventFacade = eventFacade;
        this.eventMapper = eventMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @Override
    public ResponseEntity<EventsResponseDto> getAllEvents() {
        Collection<Event> events = eventFacade.readAll();
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
        eventFacade.create(event);
        return ResponseEntity.status(201).build();
    }

    @Override
    public ResponseEntity<Void> attendEvent(Long id, AttendEventRequestDto request) {
        User user = currentUserDetailService.getFullCurrentUser();
        eventFacade.attend(id, user.getId(), request.getStatus().name());
        return ResponseEntity.status(201).build();
    }
}
