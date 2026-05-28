package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventCommentTest {

    @Test
    void validateDeletionAllowsAuthor() {
        EventComment comment = commentWithAuthor(1L);

        assertDoesNotThrow(() -> comment.validateDeletion(user(1L, UserRole.USER)));
    }

    @Test
    void validateDeletionAllowsAdmin() {
        EventComment comment = commentWithAuthor(1L);

        assertDoesNotThrow(() -> comment.validateDeletion(user(2L, UserRole.ADMIN)));
    }

    @Test
    void validateDeletionRejectsOtherUser() {
        EventComment comment = commentWithAuthor(1L);

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> comment.validateDeletion(user(2L, UserRole.USER)));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        assertEquals("Only the comment author or an admin can delete a comment", ex.getMessage());
    }

    private EventComment commentWithAuthor(Long userId) {
        EventComment comment = new EventComment();
        comment.setUser(user(userId, UserRole.USER));
        comment.setContent("Test");
        return comment;
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
