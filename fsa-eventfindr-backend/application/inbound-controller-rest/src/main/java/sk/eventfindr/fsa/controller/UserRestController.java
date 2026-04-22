package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/users/organizers")
    public ResponseEntity<List<UserDto>> getOrganizers() {
        Collection<User> organizers = userFacade.getOrganizers();
        List<UserDto> dtos = organizers.stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/users/me/become-organizer")
    public ResponseEntity<Void> becomeOrganizer() {
        String email = currentUserDetailService.getUserEmail();
        userFacade.becomeOrganizer(email);
        return ResponseEntity.ok().build();
    }
}
