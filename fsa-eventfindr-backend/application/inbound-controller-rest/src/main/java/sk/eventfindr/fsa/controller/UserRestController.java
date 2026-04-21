package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.mapper.UserMapper;
import sk.eventfindr.fsa.rest.api.UsersApi;
import sk.eventfindr.fsa.rest.dto.CreateUserRequestDto;

@RestController
public class UserRestController implements UsersApi {

    private final UserFacade userFacade;
    private final UserMapper userMapper;

    public UserRestController(UserFacade userFacade, UserMapper userMapper) {
        this.userFacade = userFacade;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<Void> createUser(CreateUserRequestDto request) {
        User user = userMapper.toEntity(request);
        userFacade.create(user);
        return ResponseEntity.status(201).build();
    }
}
