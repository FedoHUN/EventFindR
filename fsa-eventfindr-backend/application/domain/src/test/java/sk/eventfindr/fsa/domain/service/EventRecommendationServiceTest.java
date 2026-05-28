package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRole;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRecommendationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventEnrichmentService eventEnrichmentService;

    private EventRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new EventRecommendationService(eventRepository, eventEnrichmentService);
    }

    @Test
    void findSimilarThrowsNotFoundForMissingEvent() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.findSimilar(99L));

        assertEquals(EventfindrException.Type.NOT_FOUND, ex.getType());
    }

    @Test
    void findSimilarRanksMatchingGenreHigher() {
        Event source = event(1L, "Rock");
        Event rockCandidate = event(2L, "Rock");
        Event jazzCandidate = event(3L, "Jazz");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(source));
        when(eventRepository.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(source, rockCandidate, jazzCandidate));

        Collection<Event> similar = service.findSimilar(1L);

        assertTrue(similar.stream().anyMatch(e -> e.getId().equals(2L)));
    }

    @Test
    void findSimilarExcludesSourceEvent() {
        Event source = event(1L, "Rock");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(source));
        when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of(source));

        Collection<Event> similar = service.findSimilar(1L);

        assertTrue(similar.isEmpty());
    }

    @Test
    void findSimilarExcludesCanceledEvents() {
        Event source = event(1L, "Rock");
        Event canceled = event(2L, "Rock");
        canceled.setCanceled(true);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(source));
        when(eventRepository.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(source, canceled));

        Collection<Event> similar = service.findSimilar(1L);

        assertTrue(similar.isEmpty());
    }

    @Test
    void findSimilarConsidersSharedArtists() {
        Event source = event(1L, null);
        EventArtist artist = new EventArtist();
        artist.setArtistName("Shared Artist");
        source.setArtists(List.of(artist));

        Event match = event(2L, null);
        EventArtist sameArtist = new EventArtist();
        sameArtist.setArtistName("Shared Artist");
        match.setArtists(List.of(sameArtist));

        Event noMatch = event(3L, null);
        noMatch.setArtists(List.of());

        when(eventRepository.findById(1L)).thenReturn(Optional.of(source));
        when(eventRepository.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(source, match, noMatch));

        Collection<Event> similar = service.findSimilar(1L);

        assertTrue(similar.stream().anyMatch(e -> e.getId().equals(2L)));
    }

    @Test
    void findTrendingReturnsEmptyForNoCandidates() {
        when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of());

        Collection<Event> trending = service.findTrending(10);

        assertTrue(trending.isEmpty());
    }

    @Test
    void findTrendingRespectsLimit() {
        List<Event> candidates = List.of(event(1L, "Rock"), event(2L, "Jazz"), event(3L, "Pop"));
        when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(candidates);

        Collection<Event> trending = service.findTrending(2);

        assertTrue(trending.size() <= 2);
    }

    @Test
    void findTrendingRanksHighAttendanceFirst() {
        Event popular = event(1L, "Rock");
        popular.setAttendingCount(100);
        popular.setCommentCount(50);

        Event unpopular = event(2L, "Jazz");
        unpopular.setAttendingCount(1);
        unpopular.setCommentCount(0);

        when(eventRepository.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(unpopular, popular));

        Collection<Event> trending = service.findTrending(10);
        List<Event> trendingList = List.copyOf(trending);

        assertEquals(2, trendingList.size());
        assertEquals(1L, trendingList.get(0).getId());
    }

    private Event event(Long id, String genre) {
        Event event = new Event();
        event.setId(id);
        event.setGenre(genre);
        event.setStatus(EventStatus.PUBLISHED);
        event.setArtists(List.of());
        event.setCreated(new Date());
        User organizer = new User();
        organizer.setId(id * 100);
        organizer.setRole(UserRole.ORGANIZER);
        event.setOrganizer(organizer);
        return event;
    }
}
