package sk.eventfindr.fsa.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.rest.dto.UserDto;

import sk.eventfindr.fsa.domain.UserRole;

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
        UserDto jwt = getCurrentUser();
        User user = userFacade.get(jwt.getEmail());
        if (user == null) {
            // Auto-register user from Keycloak JWT on first login
            User newUser = new User();
            newUser.setEmail(jwt.getEmail());
            newUser.setName(jwt.getName());
            newUser.setRola(jwt.getRola() != null ? UserRole.valueOf(jwt.getRola().name()) : UserRole.USER);
            userFacade.create(newUser);
            user = userFacade.get(jwt.getEmail());
        }
        return user;
    }
}
