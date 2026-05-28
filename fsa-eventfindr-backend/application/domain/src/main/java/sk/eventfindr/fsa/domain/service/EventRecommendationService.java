package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.EventfindrException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventRecommendationService {

    private final EventRepository eventRepository;
    private final EventEnrichmentService eventEnrichmentService;

    public EventRecommendationService(EventRepository eventRepository,
                                      EventEnrichmentService eventEnrichmentService) {
        this.eventRepository = eventRepository;
        this.eventEnrichmentService = eventEnrichmentService;
    }

    public Collection<Event> findSimilar(Long eventId) {
        Event source = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event was not found"));
        eventEnrichmentService.enrichEvent(source);

        Set<String> sourceArtistNames = source.getArtists() == null
                ? Set.of()
                : source.getArtists().stream()
                .map(EventArtist::getArtistName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Long sourceOrganizerId = source.getOrganizer() != null ? source.getOrganizer().getId() : null;

        Collection<Event> candidates = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> !event.getId().equals(eventId) && !event.isCanceled())
                .toList();
        eventEnrichmentService.enrichEvents(candidates);

        return candidates.stream()
                .map(candidate -> Map.entry(candidate, computeSimilarityScore(source, candidate, sourceArtistNames, sourceOrganizerId)))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Event, Integer>comparingByValue().reversed())
                .limit(6)
                .map(Map.Entry::getKey)
                .toList();
    }

    public Collection<Event> findTrending(int limit) {
        Collection<Event> candidates = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> !event.isCanceled())
                .toList();
        eventEnrichmentService.enrichEvents(candidates);

        if (candidates.isEmpty()) {
            return List.of();
        }

        int maxAttendance = candidates.stream().mapToInt(Event::getAttendingCount).max().orElse(1);
        int maxComments = candidates.stream().mapToInt(Event::getCommentCount).max().orElse(1);
        long now = System.currentTimeMillis();

        return candidates.stream()
                .map(event -> Map.entry(event, computeTrendingScore(event, maxAttendance, maxComments, now)))
                .sorted(Map.Entry.<Event, Double>comparingByValue().reversed())
                .limit(clampRecommendationLimit(limit))
                .map(Map.Entry::getKey)
                .toList();
    }

    private int computeSimilarityScore(Event source, Event candidate,
                                       Set<String> sourceArtistNames,
                                       Long sourceOrganizerId) {
        int score = 0;

        if (source.getGenre() != null && !source.getGenre().isBlank()
                && source.getGenre().equalsIgnoreCase(candidate.getGenre())) {
            score += 40;
        }

        if (!sourceArtistNames.isEmpty() && candidate.getArtists() != null) {
            boolean hasSharedArtist = candidate.getArtists().stream()
                    .map(EventArtist::getArtistName)
                    .filter(Objects::nonNull)
                    .anyMatch(sourceArtistNames::contains);
            if (hasSharedArtist) {
                score += 25;
            }
        }

        if (sourceOrganizerId != null && candidate.getOrganizer() != null
                && sourceOrganizerId.equals(candidate.getOrganizer().getId())) {
            score += 20;
        }

        if (source.getLocation() != null && !source.getLocation().isBlank()
                && source.getLocation().equalsIgnoreCase(candidate.getLocation())) {
            score += 15;
        }

        if (source.getPrice() != null && candidate.getPrice() != null
                && source.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            double ratio = candidate.getPrice().doubleValue() / source.getPrice().doubleValue();
            if (ratio >= 0.7 && ratio <= 1.3) {
                score += 10;
            }
        }

        return score;
    }

    private double computeTrendingScore(Event event, int maxAttendance, int maxComments, long now) {
        double score = 0;

        if (maxAttendance > 0) {
            score += 40.0 * event.getAttendingCount() / maxAttendance;
        }
        if (event.getAverageRating() != null) {
            score += event.getAverageRating() * 8.0;
        }
        if (maxComments > 0) {
            score += 10.0 * event.getCommentCount() / maxComments;
        }
        if (event.getCreated() != null) {
            long ageDays = (now - event.getCreated().getTime()) / (1000L * 60 * 60 * 24);
            if (ageDays <= 7) {
                score += 10.0;
            } else if (ageDays < 30) {
                score += 10.0 * (30 - ageDays) / 23.0;
            }
        }

        return score;
    }

    private int clampRecommendationLimit(int limit) {
        return Math.max(1, Math.min(limit, 50));
    }
}
