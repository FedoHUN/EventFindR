package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserFollow;
import sk.eventfindr.fsa.domain.UserFollowRepository;
import sk.eventfindr.fsa.domain.UserRepository;

import java.time.Clock;
import java.util.Collection;
import java.util.Date;

public class FollowService implements FollowFacade {

    private final UserFollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationFacade notificationFacade;
    private final Clock clock;
    private final DomainLogger log;

    public FollowService(UserFollowRepository followRepository,
                         UserRepository userRepository,
                         NotificationFacade notificationFacade) {
        this(followRepository, userRepository, notificationFacade, Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public FollowService(UserFollowRepository followRepository,
                         UserRepository userRepository,
                         NotificationFacade notificationFacade,
                         Clock clock,
                         DomainLogger log) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationFacade = notificationFacade;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public void follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            log.warn("Rejected self-follow attempt for user {}", followerId);
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "You cannot follow yourself");
        }

        User follower = userRepository.get(followerId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Follower was not found"));

        User followed = userRepository.get(followedId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User to follow was not found"));

        if (followRepository.findByFollowerAndFollowed(followerId, followedId).isPresent()) {
            log.warn("Rejected duplicate follow from {} to {}", followerId, followedId);
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "You already follow this user");
        }

        UserFollow follow = new UserFollow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setCreated(Date.from(clock.instant()));
        followRepository.save(follow);
        notificationFacade.notifyUserOfNewFollower(followedId, follower.getName());
        log.info("User {} followed user {}", followerId, followedId);
    }

    @Override
    public void unfollow(Long followerId, Long followedId) {
        UserFollow follow = followRepository.findByFollowerAndFollowed(followerId, followedId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Follow relationship was not found"));
        followRepository.delete(follow);
        log.info("User {} unfollowed user {}", followerId, followedId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.findByFollowerAndFollowed(followerId, followedId).isPresent();
    }

    @Override
    public Collection<UserFollow> getFollowing(Long userId) {
        return followRepository.findByFollower(userId);
    }

    @Override
    public Collection<UserFollow> getFollowers(Long userId) {
        return followRepository.findByFollowed(userId);
    }

    @Override
    public int getFollowerCount(Long userId) {
        return followRepository.countByFollowed(userId);
    }
}
