package sk.eventfindr.fsa.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.EventMediaFacade;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/events/{eventId}/media")
public class EventMediaRestController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp",
            "video/mp4", "video/quicktime"
    );

    private final EventMediaFacade eventMediaFacade;
    private final CurrentUserDetailService currentUserDetailService;

    public EventMediaRestController(EventMediaFacade eventMediaFacade,
                                    CurrentUserDetailService currentUserDetailService) {
        this.eventMediaFacade = eventMediaFacade;
        this.currentUserDetailService = currentUserDetailService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventMediaResponseDto> uploadMedia(
            @PathVariable("eventId") Long eventId,
            @RequestParam("file") MultipartFile file) {

        User organizer = currentUserDetailService.getFullCurrentUser();

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Failed to read uploaded file");
        }

        EventMedia media = eventMediaFacade.uploadMedia(
                eventId,
                organizer.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                data);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(eventId, media));
    }

    @GetMapping
    public ResponseEntity<List<EventMediaResponseDto>> listMedia(@PathVariable("eventId") Long eventId) {
        Collection<EventMedia> mediaList = eventMediaFacade.getMediaForEvent(eventId);
        List<EventMediaResponseDto> result = mediaList.stream()
                .map(media -> toResponse(eventId, media))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{mediaId}/file")
    public ResponseEntity<byte[]> getMediaFile(
            @PathVariable("eventId") Long eventId,
            @PathVariable("mediaId") Long mediaId) {

        EventMedia media = eventMediaFacade.getMediaForEvent(eventId).stream()
                .filter(m -> m.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Media not found"));

        String contentType = media.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Unsupported media content type");
        }

        byte[] fileData = eventMediaFacade.getMediaFile(mediaId);

        String disposition = contentType.startsWith("image/") ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", disposition)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(fileData);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable("eventId") Long eventId,
            @PathVariable("mediaId") Long mediaId) {

        User organizer = currentUserDetailService.getFullCurrentUser();
        eventMediaFacade.deleteMedia(mediaId, organizer.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderMedia(@PathVariable("eventId") Long eventId,
                                             @RequestBody List<Long> mediaIds) {
        User organizer = currentUserDetailService.getFullCurrentUser();
        eventMediaFacade.reorderMedia(eventId, organizer.getId(), mediaIds);
        return ResponseEntity.ok().build();
    }

    private EventMediaResponseDto toResponse(Long eventId, EventMedia media) {
        return new EventMediaResponseDto(
                media.getId(),
                media.getMediaType().name(),
                media.getContentType(),
                media.getSortOrder(),
                "/events/" + eventId + "/media/" + media.getId() + "/file"
        );
    }

    public record EventMediaResponseDto(Long id,
                                        String mediaType,
                                        String contentType,
                                        int sortOrder,
                                        String url) {
    }
}
