package sk.eventfindr.fsa.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserFollowRepository;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserFollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationFacade notificationFacade;

    @InjectMocks
    private FollowService service;

    @Test
    void followRejectsSelfFollow() {
        EventfindrException ex = assertThrows(EventfindrException.class, () -> service.follow(1L, 1L));
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void followPersistsRelation() {
        when(userRepository.get(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.get(2L)).thenReturn(Optional.of(user(2L)));
        when(followRepository.findByFollowerAndFollowed(1L, 2L)).thenReturn(Optional.empty());

        service.follow(1L, 2L);

        verify(followRepository).save(any());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(UserRole.USER);
        user.setName("user-" + id);
        return user;
    }
}
