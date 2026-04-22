package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserSpringDataRepository userSpringDataRepository;

    public JpaUserRepositoryAdapter(UserSpringDataRepository userSpringDataRepository) {
        this.userSpringDataRepository = userSpringDataRepository;
    }

    @Override
    public Optional<User> get(long id) {
        return userSpringDataRepository.findById(id);
    }

    @Override
    public Optional<User> get(String email) {
        return userSpringDataRepository.findByEmail(email);
    }

    @Override
    public Collection<User> findByRole(UserRole role) {
        return userSpringDataRepository.findByRola(role);
    }

    @Override
    public void create(User user) {
        userSpringDataRepository.save(user);
    }

    @Override
    public void update(User user) {
        userSpringDataRepository.save(user);
    }
}
