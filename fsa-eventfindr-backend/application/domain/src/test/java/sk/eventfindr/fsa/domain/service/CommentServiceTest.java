package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.EventCommentRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private EventCommentRepository commentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationFacade notificationFacade;

    @InjectMocks
    private CommentService service;

    @Test
    void addCommentPersistsComment() {
        Event event = new Event();
        event.setOrganizer(user(1L, UserRole.ORGANIZER));
        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(userRepository.get(2L)).thenReturn(Optional.of(user(2L, UserRole.USER)));

        EventComment comment = service.addComment(5L, 2L, "Nice event", 5);

        assertEquals("Nice event", comment.getContent());
        verify(commentRepository).save(any(EventComment.class));
    }

    @Test
    void deleteCommentRejectsUnauthorizedUser() {
        EventComment comment = new EventComment();
        comment.setUser(user(1L, UserRole.USER));
        when(commentRepository.findById(9L)).thenReturn(Optional.of(comment));
        when(userRepository.get(2L)).thenReturn(Optional.of(user(2L, UserRole.USER)));

        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.deleteComment(9L, 2L));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("user" + id + "@eventfindr.sk");
        user.setName("user-" + id);
        return user;
    }
}
