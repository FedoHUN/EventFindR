package sk.eventfindr.fsa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import sk.eventfindr.fsa.domain.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtConverterTest {

    @Test
    void convertsRealmRolesCaseInsensitively() {
        JwtConverter converter = new JwtConverter(jwt(Map.of("roles", List.of("default-roles", "artist"))));

        AuthenticatedUser principal = (AuthenticatedUser) converter.getPrincipal();

        assertEquals(UserRole.ARTIST, principal.role());
        assertTrue(converter.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ARTIST")));
    }

    @Test
    void convertsPrefixedResourceAccessRoles() {
        Jwt jwt = jwt(
                Map.of("roles", List.of("offline_access")),
                Map.of("eventfindr-frontend", Map.of("roles", List.of("ROLE_ORGANIZER"))));

        JwtConverter converter = new JwtConverter(jwt);

        AuthenticatedUser principal = (AuthenticatedUser) converter.getPrincipal();

        assertEquals(UserRole.ORGANIZER, principal.role());
        assertTrue(converter.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER")));
    }

    private Jwt jwt(Map<String, Object> realmAccess) {
        return jwt(realmAccess, Map.of());
    }

    private Jwt jwt(Map<String, Object> realmAccess, Map<String, Object> resourceAccess) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", "user@example.com")
                .claim("given_name", "User")
                .claim("realm_access", realmAccess)
                .claim("resource_access", resourceAccess)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
