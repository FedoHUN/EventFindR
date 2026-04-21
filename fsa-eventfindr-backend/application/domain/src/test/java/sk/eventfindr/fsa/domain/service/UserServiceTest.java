package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService service;

    @Test
    void createPersistsUserWhenEmailIsUnique() {
        User user = user(1L, "user@eventfindr.sk");
        when(userRepository.get("user@eventfindr.sk")).thenReturn(Optional.empty());

        service.create(user);

        verify(userRepository).get("user@eventfindr.sk");
        verify(userRepository).create(user);
    }

    @Test
    void createFailsWhenEmailAlreadyExists() {
        User existing = user(1L, "user@eventfindr.sk");
        User duplicate = user(2L, "user@eventfindr.sk");
        when(userRepository.get("user@eventfindr.sk")).thenReturn(Optional.of(existing));

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.create(duplicate));

        assertEquals(EventfindrException.Type.CONFLICT, ex.getType());
        verify(userRepository).get("user@eventfindr.sk");
        verify(userRepository, never()).create(duplicate);
    }

    @Test
    void createFailsWhenEmailMissing() {
        User user = user(1L, " ");

        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.create(user));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        verify(userRepository, never()).get(" ");
        verify(userRepository, never()).create(user);
    }

    @Test
    void createFailsWhenUserIsNull() {
        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> service.create(null));

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("user-" + id);
        user.setEmail(email);
        user.setRola(UserRole.USER);
        return user;
    }
}
