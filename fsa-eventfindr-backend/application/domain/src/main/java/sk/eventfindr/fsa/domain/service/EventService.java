package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventArtistRepository;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventMediaRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.time.Clock;
import java.util.Collection;
import java.util.List;

public class EventService implements EventFacade {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventArtistRepository eventArtistRepository;
    private final EventMediaRepository eventMediaRepository;
    private final NotificationFacade notificationFacade;
    private final EventEnrichmentService eventEnrichmentService;
    private final EventMediaService eventMediaService;
    private final Clock clock;
    private final DomainLogger log;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventMediaRepository eventMediaRepository,
                        EventArtistRepository eventArtistRepository,
                        NotificationFacade notificationFacade,
                        EventEnrichmentService eventEnrichmentService,
                        EventMediaService eventMediaService) {
        this(eventRepository, userRepository, eventMediaRepository, eventArtistRepository, notificationFacade,
                eventEnrichmentService, eventMediaService, Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventMediaRepository eventMediaRepository,
                        EventArtistRepository eventArtistRepository,
                        NotificationFacade notificationFacade,
                        EventEnrichmentService eventEnrichmentService,
                        EventMediaService eventMediaService,
                        Clock clock,
                        DomainLogger log) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventArtistRepository = eventArtistRepository;
        this.eventMediaRepository = eventMediaRepository;
        this.notificationFacade = notificationFacade;
        this.eventEnrichmentService = eventEnrichmentService;
        this.eventMediaService = eventMediaService;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public Collection<Event> readAll() {
        Collection<Event> events = eventRepository.readAll();
        eventEnrichmentService.enrichEvents(events);
        return events;
    }

    @Override
    public Collection<Event> readAllPublished() {
        Collection<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED);
        eventEnrichmentService.enrichEvents(events);
        return events;
    }

    @Override
    public Event getById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event with ID " + id + " was not found"));
        eventEnrichmentService.enrichEvent(event);
        return event;
    }

    @Override
    public Long create(Event event) {
        if (event == null) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event is required");
        }

        User organizer = event.getOrganizer();
        if (organizer == null || (organizer.getRole() != UserRole.ORGANIZER && organizer.getRole() != UserRole.ADMIN)) {
            log.warn("Rejected event creation because organizer role was invalid");
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only organizers or admins can create events");
        }

        List<EventArtist> artists = event.getArtists();
        event.prepareForCreation(clock.instant());
        eventRepository.create(event);
        saveArtists(event.getId(), artists);

        if (event.getStatus() == EventStatus.PUBLISHED) {
            notificationFacade.notifyFollowersOfNewEvent(organizer.getId(), event.getId(), event.getName());
        }
        log.info("Created event {} by organizer {}", event.getId(), organizer.getId());
        return event.getId();
    }

    @Override
    public void update(Event updated, Long userId) {
        Event existing = eventRepository.findById(updated.getId())
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event with ID " + updated.getId() + " was not found"));
        User user = getRequiredUser(userId);

        existing.validateForUpdate(user);
        applyUpdates(existing, updated);
        existing.validate();
        eventRepository.update(existing);

        if (updated.getArtists() != null) {
            eventArtistRepository.deleteByEventId(existing.getId());
            saveArtists(existing.getId(), updated.getArtists());
        }

        if (existing.getStatus() == EventStatus.PUBLISHED) {
            notificationFacade.notifyAttendeesOfUpdate(existing.getId(), existing.getName());
        }
        log.info("Updated event {} by user {}", existing.getId(), userId);
    }

    @Override
    public void cancelEvent(Long eventId, Long userId) {
        Event event = getRequiredEvent(eventId);
        User user = getRequiredUser(userId);

        event.cancel(user);
        eventRepository.update(event);
        notificationFacade.notifyAttendeesOfCancellation(eventId, event.getName());
        log.info("Canceled event {} by user {}", eventId, userId);
    }

    @Override
    public void restoreEvent(Long eventId, Long userId) {
        Event event = getRequiredEvent(eventId);
        User user = getRequiredUser(userId);

        event.restore(user);
        eventRepository.update(event);
        log.info("Restored event {} by user {}", eventId, userId);
    }

    @Override
    public void deleteEvent(Long eventId, Long userId) {
        Event event = getRequiredEvent(eventId);
        User user = getRequiredUser(userId);
        event.validateForUpdate(user);

        for (EventMedia media : eventMediaRepository.findByEventId(eventId)) {
            eventMediaService.deleteMedia(media.getId(), userId);
        }
        eventArtistRepository.deleteByEventId(eventId);
        eventRepository.delete(eventId);
        log.info("Deleted event {} by user {}", eventId, userId);
    }

    @Override
    public void publishEvent(Long eventId, Long userId) {
        Event event = getRequiredEvent(eventId);
        User user = getRequiredUser(userId);

        event.publish(user);
        eventRepository.update(event);
        notificationFacade.notifyFollowersOfNewEvent(event.getOrganizer().getId(), eventId, event.getName());
        log.info("Published event {} by user {}", eventId, userId);
    }

    @Override
    public void toggleFeatured(Long eventId, Long userId) {
        Event event = getRequiredEvent(eventId);
        User user = getRequiredUser(userId);

        event.toggleFeatured(user);
        eventRepository.update(event);
        log.info("Toggled featured state for event {} by user {}", eventId, userId);
    }

    private Event getRequiredEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event with ID " + eventId + " was not found"));
    }

    private User getRequiredUser(Long userId) {
        return userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));
    }

    private void applyUpdates(Event existing, Event updated) {
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setEventDate(updated.getEventDate());
        existing.setPrice(updated.getPrice());
        existing.setTicketUrl(updated.getTicketUrl());
        existing.setImageUrl(updated.getImageUrl());
        existing.setGenre(updated.getGenre());
        existing.setCapacity(updated.getCapacity());
    }

    private void saveArtists(Long eventId, List<EventArtist> artists) {
        if (artists == null) {
            return;
        }
        for (int index = 0; index < artists.size(); index++) {
            EventArtist eventArtist = artists.get(index);
            eventArtist.setEventId(eventId);
            eventArtist.setSortOrder(index);
            eventArtistRepository.save(eventArtist);
        }
    }
}
