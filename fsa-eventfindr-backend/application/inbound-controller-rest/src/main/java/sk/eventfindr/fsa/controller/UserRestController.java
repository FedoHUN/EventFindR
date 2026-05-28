package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.mapper.UserMapper;
import sk.eventfindr.fsa.rest.api.UsersApi;
import sk.eventfindr.fsa.rest.dto.CreateUserRequestDto;
import sk.eventfindr.fsa.rest.dto.UserDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;
import java.util.List;

@RestController
public class UserRestController implements UsersApi {

    private final UserFacade userFacade;
    private final UserMapper userMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public UserRestController(UserFacade userFacade,
                              UserMapper userMapper,
                              CurrentUserDetailService currentUserDetailService) {
        this.userFacade = userFacade;
        this.userMapper = userMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @Override
    public ResponseEntity<Void> createUser(CreateUserRequestDto request) {
        User user = userMapper.toEntity(request);
        userFacade.create(user);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getMe() {
        User user = currentUserDetailService.getFullCurrentUser();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id) {
        User user = userFacade.get(id)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/users/organizers")
    public ResponseEntity<List<UserDto>> getOrganizers() {
        Collection<User> organizers = userFacade.getOrganizers();
        List<UserDto> dtos = organizers.stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/users/me/become-organizer")
    public ResponseEntity<Void> becomeOrganizer(@RequestBody BecomeOrganizerRequest request) {
        String email = currentUserDetailService.getUserEmail();
        userFacade.becomeOrganizer(email, request.organizationName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/me/become-artist")
    public ResponseEntity<Void> becomeArtist(@RequestBody BecomeArtistRequest request) {
        String email = currentUserDetailService.getUserEmail();
        userFacade.becomeArtist(email, request.artistName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/me/organization-name")
    public ResponseEntity<Void> updateOrganizationName(@RequestBody UpdateOrganizationNameRequest request) {
        String email = currentUserDetailService.getUserEmail();
        userFacade.updateOrganizationName(email, request.organizationName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/me/organization-description")
    public ResponseEntity<Void> updateOrganizationDescription(@RequestBody UpdateOrganizationDescriptionRequest request) {
        String email = currentUserDetailService.getUserEmail();
        userFacade.updateOrganizationDescription(email, request.organizationDescription());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/me/artist-description")
    public ResponseEntity<Void> updateArtistDescription(@RequestBody UpdateArtistDescriptionRequest request) {
        String email = currentUserDetailService.getUserEmail();
        userFacade.updateArtistDescription(email, request.artistDescription());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/artists/search")
    public ResponseEntity<List<UserDto>> searchArtists(@RequestParam(value = "q", required = false, defaultValue = "") String query) {
        Collection<User> artists = userFacade.searchArtists(query);
        List<UserDto> dtos = artists.stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    record BecomeOrganizerRequest(String organizationName) {}
    record BecomeArtistRequest(String artistName) {}
    record UpdateOrganizationNameRequest(String organizationName) {}
    record UpdateOrganizationDescriptionRequest(String organizationDescription) {}
    record UpdateArtistDescriptionRequest(String artistDescription) {}
}
