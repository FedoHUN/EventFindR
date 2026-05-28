package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventArtistRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventDiscoveryService implements EventDiscoveryFacade {

    private final EventRepository eventRepository;
    private final EventArtistRepository eventArtistRepository;
    private final EventEnrichmentService eventEnrichmentService;
    private final EventRecommendationService eventRecommendationService;

    public EventDiscoveryService(EventRepository eventRepository,
                                 EventArtistRepository eventArtistRepository,
                                 EventEnrichmentService eventEnrichmentService,
                                 EventRecommendationService eventRecommendationService) {
        this.eventRepository = eventRepository;
        this.eventArtistRepository = eventArtistRepository;
        this.eventEnrichmentService = eventEnrichmentService;
        this.eventRecommendationService = eventRecommendationService;
    }

    @Override
    public Collection<Event> findByArtistUserId(Long artistUserId) {
        Collection<EventArtist> artistEntries = eventArtistRepository.findByArtistUserId(artistUserId);
        Set<Long> eventIds = new LinkedHashSet<>();
        for (EventArtist eventArtist : artistEntries) {
            eventIds.add(eventArtist.getEventId());
        }

        Collection<Event> events = eventRepository.findByIds(eventIds);
        eventEnrichmentService.enrichEvents(events);
        return events;
    }

    @Override
    public Collection<Event> findDraftsByOrganizer(Long organizerId) {
        Collection<Event> drafts = eventRepository.findByOrganizerIdAndStatus(organizerId, EventStatus.DRAFT);
        eventEnrichmentService.enrichEvents(drafts);
        return drafts;
    }

    @Override
    public Collection<Event> findSimilar(Long eventId) {
        return eventRecommendationService.findSimilar(eventId);
    }

    @Override
    public Collection<Event> findTrending(int limit) {
        return eventRecommendationService.findTrending(limit);
    }
}
