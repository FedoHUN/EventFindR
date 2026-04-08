package sk.eventfindr.fsa.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.rest.dto.UserDto;

import java.util.List;

@Service
public class CurrentUserDetailService {

    private final UserFacade userFacade;

    public CurrentUserDetailService(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    public UserDto getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDto) {
            return (UserDto) principal;
        }

        throw new EventfindrException(
                EventfindrException.Type.UNAUTHORIZED,
                "Authentication required",
                List.of("Current principal is not a valid API user"));
    }

    public String getUserEmail() {
        return getCurrentUser().getEmail();
    }

    public User getFullCurrentUser() {
        User user = userFacade.get(getUserEmail());
        if (user == null) {
            throw new EventfindrException(
                    EventfindrException.Type.UNAUTHORIZED,
                    "Authenticated user not available in local user repository",
                    List.of("email=" + getUserEmail()));
        }
        return user;
    }
}
