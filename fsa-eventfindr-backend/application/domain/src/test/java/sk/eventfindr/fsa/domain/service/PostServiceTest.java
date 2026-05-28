package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.MediaStorage;
import sk.eventfindr.fsa.domain.MediaType;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.PostMediaRepository;
import sk.eventfindr.fsa.domain.PostRepository;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;
import sk.eventfindr.fsa.domain.VideoCompressor;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaStorage mediaStorage;

    @Mock
    private VideoCompressor videoCompressor;

    private PostService service;

    @BeforeEach
    void setUp() {
        service = new PostService(postRepository, postMediaRepository, userRepository, mediaStorage, videoCompressor);
    }

    @Test
    void createPostRejectsDisallowedRole() {
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L, UserRole.USER)));

        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.createPost(1L, "Hello"));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
    }

    @Test
    void createPostPersistsForOrganizer() {
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L, UserRole.ORGANIZER)));

        service.createPost(1L, "Hello");

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void uploadMediaPersistsForAuthorWithAllowedRole() {
        when(postRepository.findById(2L)).thenReturn(Optional.of(post(2L, user(1L, UserRole.ORGANIZER))));
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L, UserRole.ORGANIZER)));
        when(postMediaRepository.countByPostIdAndMediaType(2L, MediaType.IMAGE)).thenReturn(0);
        when(postMediaRepository.findByPostId(2L)).thenReturn(List.of());

        PostMedia media = service.uploadMedia(2L, 1L, "photo.jpg", "image/jpeg", 1024, new byte[1024]);

        assertNotNull(media);
        assertEquals(MediaType.IMAGE, media.getMediaType());
        verify(mediaStorage).store(any(byte[].class), anyString());
        verify(postMediaRepository).save(any(PostMedia.class));
    }

    @Test
    void uploadMediaRejectsAuthorWithDisallowedCurrentRole() {
        when(postRepository.findById(2L)).thenReturn(Optional.of(post(2L, user(1L, UserRole.ORGANIZER))));
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L, UserRole.USER)));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.uploadMedia(2L, 1L, "photo.jpg", "image/jpeg", 1024, new byte[1024]));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        verify(mediaStorage, never()).store(any(), anyString());
    }

    @Test
    void uploadMediaDeletesStoredFileWhenPersistenceFails() {
        when(postRepository.findById(2L)).thenReturn(Optional.of(post(2L, user(1L, UserRole.ARTIST))));
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L, UserRole.ARTIST)));
        when(postMediaRepository.countByPostIdAndMediaType(2L, MediaType.IMAGE)).thenReturn(0);
        when(postMediaRepository.findByPostId(2L)).thenReturn(List.of());
        doThrow(new IllegalStateException("db failed")).when(postMediaRepository).save(any(PostMedia.class));

        assertThrows(IllegalStateException.class,
                () -> service.uploadMedia(2L, 1L, "photo.jpg", "image/jpeg", 1024, new byte[1024]));

        verify(mediaStorage).store(any(byte[].class), anyString());
        verify(mediaStorage).delete(anyString());
    }

    private Post post(Long id, User author) {
        Post post = new Post();
        post.setId(id);
        post.setAuthor(author);
        return post;
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("user" + id + "@eventfindr.sk");
        return user;
    }
}
