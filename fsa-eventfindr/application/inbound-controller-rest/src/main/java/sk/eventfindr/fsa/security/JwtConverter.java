package sk.eventfindr.fsa.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import sk.eventfindr.fsa.rest.dto.UserDto;
import sk.eventfindr.fsa.rest.dto.UserRoleDto;

import java.util.*;

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
        UserDto userDto = new UserDto();
        userDto.setEmail(source.getClaimAsString("email"));
        userDto.setName(source.getClaimAsString("given_name"));
        userDto.setRola(getRole());
        return userDto;
    }

    private UserRoleDto getRole() {
        Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) return null;

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        Optional<UserRoleDto> userRoleDto = findRole(roles);

        return userRoleDto.orElse(null);
    }

    private Optional<UserRoleDto> findRole(List<String> roles) {
        return roles.stream()
                .filter(role -> Arrays.stream(UserRoleDto.values())
                        .anyMatch(enumRole -> enumRole.name().equals(role)))
                .map(UserRoleDto::fromValue)
                .findFirst();
    }

    private static Collection<? extends GrantedAuthority> toAuthorities(Jwt source) {
        Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
