package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.UserFollow;

import java.util.Collection;
import java.util.Optional;

interface UserFollowSpringDataRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    Collection<UserFollow> findByFollowerId(Long followerId);

    Collection<UserFollow> findByFollowedId(Long followedId);

    int countByFollowedId(Long followedId);
}
