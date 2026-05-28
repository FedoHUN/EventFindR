package sk.eventfindr.fsa.jpa;

import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.UserFollow;
import sk.eventfindr.fsa.domain.UserFollowRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class JpaUserFollowRepositoryAdapter implements UserFollowRepository {

    private final UserFollowSpringDataRepository repository;

    public JpaUserFollowRepositoryAdapter(UserFollowSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UserFollow follow) {
        repository.save(follow);
    }

    @Override
    public Optional<UserFollow> findByFollowerAndFollowed(Long followerId, Long followedId) {
        return repository.findByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public Collection<UserFollow> findByFollower(Long followerId) {
        return repository.findByFollowerId(followerId);
    }

    @Override
    public Collection<UserFollow> findByFollowed(Long followedId) {
        return repository.findByFollowedId(followedId);
    }

    @Override
    public int countByFollowed(Long followedId) {
        return repository.countByFollowedId(followedId);
    }

    @Override
    public void delete(UserFollow follow) {
        repository.delete(follow);
    }
}
