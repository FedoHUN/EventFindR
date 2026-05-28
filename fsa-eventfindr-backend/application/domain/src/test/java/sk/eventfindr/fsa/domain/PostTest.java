package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostTest {

    @Test
    void validateDeletionAllowsAuthor() {
        Post post = postWithAuthor(1L);

        assertDoesNotThrow(() -> post.validateDeletion(user(1L, UserRole.USER)));
    }

    @Test
    void validateDeletionAllowsAdmin() {
        Post post = postWithAuthor(1L);

        assertDoesNotThrow(() -> post.validateDeletion(user(2L, UserRole.ADMIN)));
    }

    @Test
    void validateDeletionRejectsOtherUser() {
        Post post = postWithAuthor(1L);

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> post.validateDeletion(user(2L, UserRole.USER)));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        assertEquals("Only the post author or an admin can delete a post", ex.getMessage());
    }

    private Post postWithAuthor(Long userId) {
        Post post = new Post();
        post.setAuthor(user(userId, UserRole.USER));
        return post;
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
