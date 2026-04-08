package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;

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
    public void create(User user) {
        userSpringDataRepository.save(user);
    }
}
