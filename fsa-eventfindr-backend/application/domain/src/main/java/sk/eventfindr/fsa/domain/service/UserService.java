package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;

public class UserService implements UserFacade {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User get(long id) {
        return userRepository.get(id).orElse(null);
    }

    @Override
    public User get(String email) {
        return userRepository.get(email).orElse(null);
    }

    @Override
    public void create(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Email používateľa je povinný údaj");
        }
        if (userRepository.get(user.getEmail()).isPresent()) {
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "Používateľ s daným emailom už existuje");
        }
        userRepository.create(user);
    }
}
