package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.Optional;

public interface UserFollowRepository {

    void save(UserFollow follow);

    Optional<UserFollow> findByFollowerAndFollowed(Long followerId, Long followedId);

    Collection<UserFollow> findByFollower(Long followerId);

    Collection<UserFollow> findByFollowed(Long followedId);

    int countByFollowed(Long followedId);

    void delete(UserFollow follow);
}
