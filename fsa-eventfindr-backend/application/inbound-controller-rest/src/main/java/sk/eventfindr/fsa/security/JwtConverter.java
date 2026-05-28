package sk.eventfindr.fsa.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.*;
import java.util.stream.Stream;

class JwtConverter extends AbstractAuthenticationToken {
    private final Jwt source;

    public JwtConverter(Jwt source) {
        super(toAuthorities(source));
        this.source = Objects.requireNonNull(source);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return Collections.emptyList();
    }

    @Override
    public Object getPrincipal() {
        return new AuthenticatedUser(
                source.getClaimAsString("email"),
                source.getClaimAsString("given_name"),
                getRole()
        );
    }

    private UserRole getRole() {
        return findRole(extractRoles(source)).orElse(null);
    }

    private Optional<UserRole> findRole(Collection<String> roles) {
        return roles.stream()
                .map(JwtConverter::normalizeRole)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Collection<? extends GrantedAuthority> toAuthorities(Jwt source) {
        return extractRoles(source).stream()
                .map(JwtConverter::normalizeRole)
                .flatMap(Optional::stream)
                .map(UserRole::name)
                .distinct()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    private static Collection<String> extractRoles(Jwt source) {
        List<String> roles = new ArrayList<>();
        roles.addAll(extractRolesFromAccessClaim(source.getClaimAsMap("realm_access")));

        Map<String, Object> resourceAccess = source.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().stream()
                    .filter(Map.class::isInstance)
                    .map(value -> (Map<?, ?>) value)
                    .forEach(access -> roles.addAll(extractRolesFromAccessClaim(access)));
        }

        return roles;
    }

    private static Collection<String> extractRolesFromAccessClaim(Map<?, ?> accessClaim) {
        if (accessClaim == null) {
            return List.of();
        }
        Object rawRoles = accessClaim.get("roles");
        if (!(rawRoles instanceof Collection<?> roleValues)) {
            return List.of();
        }
        return roleValues.stream()
                .flatMap(value -> value instanceof String role ? Stream.of(role) : Stream.empty())
                .toList();
    }

    private static Optional<UserRole> normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        try {
            return Optional.of(UserRole.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
