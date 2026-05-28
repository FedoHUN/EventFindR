package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventMediaServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMediaRepository eventMediaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaStorage mediaStorage;

    @Mock
    private VideoCompressor videoCompressor;

    private EventMediaService service;

    @BeforeEach
    void setUp() {
        service = new EventMediaService(
                eventRepository,
                eventMediaRepository,
                userRepository,
                mediaStorage,
                videoCompressor,
                MediaValidator.defaultValidator()
        );
    }

    @Test
    void uploadMediaStoresFileAndPersists() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(10L)).thenReturn(Optional.of(organizer(10L)));
        when(eventMediaRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventMediaRepository.countByEventIdAndMediaType(1L, MediaType.IMAGE)).thenReturn(0);

        EventMedia result = service.uploadMedia(1L, 10L, "photo.jpg", "image/jpeg", 1024, new byte[1024]);

        assertNotNull(result);
        assertEquals(MediaType.IMAGE, result.getMediaType());
        verify(mediaStorage).store(any(byte[].class), anyString());
        verify(eventMediaRepository).save(any(EventMedia.class));
    }

    @Test
    void uploadMediaRejectsNonOrganizer() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(20L)).thenReturn(Optional.of(user(20L, UserRole.USER)));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.uploadMedia(1L, 20L, "photo.jpg", "image/jpeg", 1024, new byte[1024]));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        verify(mediaStorage, never()).store(any(), anyString());
    }

    @Test
    void uploadMediaRejectsUnsupportedContentType() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(10L)).thenReturn(Optional.of(organizer(10L)));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.uploadMedia(1L, 10L, "file.txt", "text/plain", 100, new byte[100]));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void uploadMediaRejectsOversizedImage() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(10L)).thenReturn(Optional.of(organizer(10L)));

        long oversized = 6L * 1024 * 1024;
        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.uploadMedia(1L, 10L, "big.jpg", "image/jpeg", oversized, new byte[0]));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void uploadMediaRejectsWhenImageLimitReached() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(10L)).thenReturn(Optional.of(organizer(10L)));
        when(eventMediaRepository.countByEventIdAndMediaType(1L, MediaType.IMAGE)).thenReturn(9);

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.uploadMedia(1L, 10L, "photo.jpg", "image/jpeg", 1024, new byte[1024]));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void deleteMediaRemovesFileAndRecord() {
        EventMedia media = new EventMedia();
        media.setId(5L);
        media.setEventId(1L);
        media.setFileName("stored.jpg");

        Event event = eventWithOrganizer(1L, 10L);
        when(eventMediaRepository.findById(5L)).thenReturn(Optional.of(media));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(10L)).thenReturn(Optional.of(organizer(10L)));

        service.deleteMedia(5L, 10L);

        verify(mediaStorage).delete("stored.jpg");
        verify(eventMediaRepository).delete(media);
    }

    @Test
    void deleteMediaRejectsNonOwner() {
        EventMedia media = new EventMedia();
        media.setId(5L);
        media.setEventId(1L);

        Event event = eventWithOrganizer(1L, 10L);
        when(eventMediaRepository.findById(5L)).thenReturn(Optional.of(media));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(20L)).thenReturn(Optional.of(user(20L, UserRole.USER)));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.deleteMedia(5L, 20L));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
    }

    @Test
    void getMediaFileThrowsNotFoundForMissingMedia() {
        when(eventMediaRepository.findById(99L)).thenReturn(Optional.empty());

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.getMediaFile(99L));

        assertEquals(EventfindrException.Type.NOT_FOUND, ex.getType());
    }

    @Test
    void adminCanUploadMediaToAnyEvent() {
        Event event = eventWithOrganizer(1L, 10L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.get(99L)).thenReturn(Optional.of(user(99L, UserRole.ADMIN)));
        when(eventMediaRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventMediaRepository.countByEventIdAndMediaType(1L, MediaType.IMAGE)).thenReturn(0);

        EventMedia result = service.uploadMedia(1L, 99L, "photo.jpg", "image/jpeg", 1024, new byte[1024]);

        assertNotNull(result);
        verify(mediaStorage).store(any(byte[].class), anyString());
    }

    private Event eventWithOrganizer(Long eventId, Long organizerId) {
        Event event = new Event();
        event.setId(eventId);
        User org = new User();
        org.setId(organizerId);
        org.setRole(UserRole.ORGANIZER);
        event.setOrganizer(org);
        return event;
    }

    private User organizer(Long id) {
        return user(id, UserRole.ORGANIZER);
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("user" + id + "@eventfindr.sk");
        return user;
    }
}
