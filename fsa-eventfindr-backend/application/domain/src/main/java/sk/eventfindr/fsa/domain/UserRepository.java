package sk.eventfindr.fsa.domain;

import java.util.Optional;

public interface UserRepository {

    Optional<User> get(long id);

    Optional<User> get(String email);

    void create(User user);
}
