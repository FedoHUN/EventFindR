package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.User;

import java.util.Collection;

public interface UserFacade {

    User get(long id);

    User get(String email);

    Collection<User> getOrganizers();

    void create(User user);

    void becomeOrganizer(String email);
}
