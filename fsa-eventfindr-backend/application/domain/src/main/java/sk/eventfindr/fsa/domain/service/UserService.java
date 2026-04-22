package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Collection;

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
    public Collection<User> getOrganizers() {
        return userRepository.findByRole(UserRole.ORGANIZER);
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

    @Override
    public void becomeOrganizer(String email) {
        User user = userRepository.get(email)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Používateľ s emailom " + email + " nebol nájdený"));

        if (user.getRola() == UserRole.ORGANIZER || user.getRola() == UserRole.ADMIN) {
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "Používateľ už má rolu " + user.getRola().name());
        }

        user.setRola(UserRole.ORGANIZER);
        userRepository.update(user);
    }
}
