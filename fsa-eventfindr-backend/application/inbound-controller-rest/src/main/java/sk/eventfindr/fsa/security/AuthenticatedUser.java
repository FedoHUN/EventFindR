package sk.eventfindr.fsa.security;

import sk.eventfindr.fsa.domain.UserRole;

public record AuthenticatedUser(String email, String name, UserRole role) {

    public UserRole effectiveRole() {
        return role == null ? UserRole.USER : role;
    }
}
