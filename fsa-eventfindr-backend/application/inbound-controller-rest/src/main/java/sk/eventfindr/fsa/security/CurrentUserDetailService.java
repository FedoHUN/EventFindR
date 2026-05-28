package sk.eventfindr.fsa.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.UserFacade;

import java.util.List;

@Service
public class CurrentUserDetailService {

    private final UserFacade userFacade;

    public CurrentUserDetailService(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            return user;
        }

        throw new EventfindrException(
                EventfindrException.Type.UNAUTHORIZED,
                "Authentication required",
                List.of("Current principal is not a valid API user"));
    }

    public String getUserEmail() {
        return getCurrentUser().email();
    }

    public User getFullCurrentUser() {
        AuthenticatedUser jwt = getCurrentUser();
        return userFacade.get(jwt.email()).orElseGet(() -> {
            // Auto-register user from Keycloak JWT on first login
            User newUser = new User();
            newUser.setEmail(jwt.email());
            newUser.setName(jwt.name());
            newUser.setRole(jwt.effectiveRole());
            userFacade.create(newUser);
            return userFacade.get(jwt.email())
                    .orElseThrow(() -> new EventfindrException(
                            EventfindrException.Type.NOT_FOUND,
                            "Failed to register user"));
        });
    }
}
