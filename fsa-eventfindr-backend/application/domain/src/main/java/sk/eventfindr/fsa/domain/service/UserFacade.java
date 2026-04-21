package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.User;

public interface UserFacade {

    User get(long id);

    User get(String email);

    void create(User user);
}
