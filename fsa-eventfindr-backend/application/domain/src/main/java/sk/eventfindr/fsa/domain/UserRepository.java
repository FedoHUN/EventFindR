package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    Optional<User> get(long id);

    Optional<User> get(String email);

    Collection<User> findByRole(UserRole role);

    void create(User user);

    void update(User user);
}
