package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventArtistRepository;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventCommentRepository;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventMediaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EventEnrichmentService {

    private final EventArtistRepository eventArtistRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventCommentRepository eventCommentRepository;
    private final EventMediaRepository eventMediaRepository;

    public EventEnrichmentService(EventArtistRepository eventArtistRepository,
                                  EventAttendanceRepository eventAttendanceRepository,
                                  EventCommentRepository eventCommentRepository,
                                  EventMediaRepository eventMediaRepository) {
        this.eventArtistRepository = eventArtistRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
        this.eventCommentRepository = eventCommentRepository;
        this.eventMediaRepository = eventMediaRepository;
    }

    public void enrichEvent(Event event) {
        enrichEvents(List.of(event));
    }

    public void enrichEvents(Collection<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Event> eventList = new ArrayList<>(events);
        List<Long> eventIds = eventList.stream().map(Event::getId).toList();

        Map<Long, List<EventArtist>> artistsByEventId = safeMap(eventArtistRepository.findByEventIds(eventIds));
        Map<Long, Map<AttendanceStatus, Integer>> attendanceCounts = safeMap(eventAttendanceRepository.countsByEventIds(eventIds));
        Map<Long, Integer> commentCounts = safeMap(eventCommentRepository.countByEventIds(eventIds));
        Map<Long, Double> averageRatings = safeMap(eventCommentRepository.getAverageRatingByEventIds(eventIds));
        Map<Long, Integer> ratingCounts = safeMap(eventCommentRepository.countRatingsByEventIds(eventIds));
        Map<Long, EventMedia> coverImages = safeMap(eventMediaRepository.findFirstImageByEventIds(eventIds));

        for (Event event : eventList) {
            event.setArtists(new ArrayList<>(artistsByEventId.getOrDefault(event.getId(), List.of())));

            Map<AttendanceStatus, Integer> counts = attendanceCounts.getOrDefault(event.getId(), Map.of());
            event.setAttendingCount(counts.getOrDefault(AttendanceStatus.ATTENDING, 0));
            event.setWatchingCount(counts.getOrDefault(AttendanceStatus.WATCHING, 0));
            event.setCommentCount(commentCounts.getOrDefault(event.getId(), 0));
            event.setAverageRating(averageRatings.get(event.getId()));
            event.setRatingCount(ratingCounts.getOrDefault(event.getId(), 0));

            EventMedia coverImage = coverImages.get(event.getId());
            if ((event.getImageUrl() == null || event.getImageUrl().isBlank()) && coverImage != null) {
                event.setCoverImageMediaId(coverImage.getId());
            }
        }
    }

    private <K, V> Map<K, V> safeMap(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }
}
