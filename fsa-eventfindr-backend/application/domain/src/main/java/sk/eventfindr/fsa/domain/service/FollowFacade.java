package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.UserFollow;

import java.util.Collection;

public interface FollowFacade {

    void follow(Long followerId, Long followedId);

    void unfollow(Long followerId, Long followedId);

    boolean isFollowing(Long followerId, Long followedId);

    Collection<UserFollow> getFollowing(Long userId);

    Collection<UserFollow> getFollowers(Long userId);

    int getFollowerCount(Long userId);
}
