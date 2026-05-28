package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventMediaRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.MediaStorage;
import sk.eventfindr.fsa.domain.MediaType;
import sk.eventfindr.fsa.domain.MediaValidator;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;
import sk.eventfindr.fsa.domain.VideoCompressor;

import java.time.Clock;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventMediaService implements EventMediaFacade {

    private static final int MAX_IMAGES_PER_EVENT = 9;
    private static final int MAX_VIDEOS_PER_EVENT = 1;

    private final EventRepository eventRepository;
    private final EventMediaRepository eventMediaRepository;
    private final UserRepository userRepository;
    private final MediaStorage mediaStorage;
    private final VideoCompressor videoCompressor;
    private final MediaValidator mediaValidator;
    private final Clock clock;
    private final DomainLogger log;

    public EventMediaService(EventRepository eventRepository,
                             EventMediaRepository eventMediaRepository,
                             UserRepository userRepository,
                             MediaStorage mediaStorage,
                             VideoCompressor videoCompressor) {
        this(eventRepository, eventMediaRepository, userRepository, mediaStorage, videoCompressor,
                MediaValidator.defaultValidator(), Clock.systemDefaultZone(), DomainLogger.noop());
    }

    EventMediaService(EventRepository eventRepository,
                      EventMediaRepository eventMediaRepository,
                      UserRepository userRepository,
                      MediaStorage mediaStorage,
                      VideoCompressor videoCompressor,
                      MediaValidator mediaValidator) {
        this(eventRepository, eventMediaRepository, userRepository, mediaStorage, videoCompressor,
                mediaValidator, Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public EventMediaService(EventRepository eventRepository,
                             EventMediaRepository eventMediaRepository,
                             UserRepository userRepository,
                             MediaStorage mediaStorage,
                             VideoCompressor videoCompressor,
                             MediaValidator mediaValidator,
                             Clock clock,
                             DomainLogger log) {
        this.eventRepository = eventRepository;
        this.eventMediaRepository = eventMediaRepository;
        this.userRepository = userRepository;
        this.mediaStorage = mediaStorage;
        this.videoCompressor = videoCompressor;
        this.mediaValidator = mediaValidator;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public EventMedia uploadMedia(Long eventId, Long organizerId, String originalName,
                                  String contentType, long fileSize, byte[] data) {
        Event event = getRequiredEvent(eventId);
        validateOwnership(event, organizerId, "Only the event organizer can upload media");

        MediaType mediaType = mediaValidator.resolveMediaType(contentType);
        mediaValidator.validateFileSize(mediaType, fileSize);
        validateMediaSlotAvailability(eventId, mediaType);

        String storedFileName = UUID.randomUUID() + mediaValidator.extractExtension(originalName, contentType);
        mediaStorage.store(data, storedFileName);

        EventMedia media = new EventMedia();
        media.setEventId(eventId);
        media.setFileName(storedFileName);
        media.setOriginalName(originalName);
        media.setContentType(contentType);
        media.setMediaType(mediaType);
        media.setFileSize(fileSize);
        media.setSortOrder(eventMediaRepository.findByEventId(eventId).size());
        media.setCreated(Date.from(clock.instant()));
        try {
            eventMediaRepository.save(media);
        } catch (RuntimeException ex) {
            mediaStorage.delete(storedFileName);
            throw ex;
        }

        if (mediaType == MediaType.VIDEO) {
            videoCompressor.compress(storedFileName);
        }

        log.info("Uploaded {} media {} for event {}", mediaType, media.getId(), eventId);
        return media;
    }

    @Override
    public Collection<EventMedia> getMediaForEvent(Long eventId) {
        return eventMediaRepository.findByEventId(eventId);
    }

    @Override
    public byte[] getMediaFile(Long mediaId) {
        EventMedia media = eventMediaRepository.findById(mediaId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media was not found"));
        return mediaStorage.load(media.getFileName());
    }

    @Override
    public void deleteMedia(Long mediaId, Long organizerId) {
        EventMedia media = eventMediaRepository.findById(mediaId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media was not found"));

        Event event = getRequiredEvent(media.getEventId());
        validateOwnership(event, organizerId, "Only the event organizer can delete media");

        mediaStorage.delete(media.getFileName());
        eventMediaRepository.delete(media);
        log.info("Deleted event media {} for event {}", mediaId, event.getId());
    }

    @Override
    public void reorderMedia(Long eventId, Long organizerId, List<Long> mediaIds) {
        Event event = getRequiredEvent(eventId);
        validateOwnership(event, organizerId, "Only the event organizer can reorder media");

        for (int index = 0; index < mediaIds.size(); index++) {
            EventMedia media = eventMediaRepository.findById(mediaIds.get(index))
                    .orElseThrow(() -> new EventfindrException(
                            EventfindrException.Type.NOT_FOUND,
                            "Media was not found"));
            if (!media.getEventId().equals(eventId)) {
                throw new EventfindrException(
                        EventfindrException.Type.VALIDATION,
                        "Media does not belong to the requested event");
            }
            media.setSortOrder(index);
            eventMediaRepository.save(media);
        }
        log.info("Reordered {} media items for event {}", mediaIds.size(), eventId);
    }

    private Event getRequiredEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event was not found"));
    }

    private void validateOwnership(Event event, Long organizerId, String message) {
        User actor = userRepository.get(organizerId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));
        boolean ownsEvent = event.getOrganizer().getId().equals(organizerId);
        if (!ownsEvent && actor.getRole() != UserRole.ADMIN) {
            log.warn("Rejected event media action for event {} by user {}", event.getId(), organizerId);
            throw new EventfindrException(EventfindrException.Type.FORBIDDEN, message);
        }
    }

    private void validateMediaSlotAvailability(Long eventId, MediaType mediaType) {
        int currentCount = eventMediaRepository.countByEventIdAndMediaType(eventId, mediaType);
        if (mediaType == MediaType.IMAGE && currentCount >= MAX_IMAGES_PER_EVENT) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "An event can have at most " + MAX_IMAGES_PER_EVENT + " images");
        }
        if (mediaType == MediaType.VIDEO && currentCount >= MAX_VIDEOS_PER_EVENT) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "An event can have at most " + MAX_VIDEOS_PER_EVENT + " video");
        }
    }
}
