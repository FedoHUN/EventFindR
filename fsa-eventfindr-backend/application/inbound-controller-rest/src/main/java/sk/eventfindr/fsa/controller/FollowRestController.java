package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserFollow;
import sk.eventfindr.fsa.domain.service.FollowFacade;
import sk.eventfindr.fsa.mapper.UserMapper;
import sk.eventfindr.fsa.rest.dto.UserDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class FollowRestController {

    private final FollowFacade followFacade;
    private final UserMapper userMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public FollowRestController(FollowFacade followFacade,
                                UserMapper userMapper,
                                CurrentUserDetailService currentUserDetailService) {
        this.followFacade = followFacade;
        this.userMapper = userMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<Void> follow(@PathVariable("userId") Long userId) {
        User user = currentUserDetailService.getFullCurrentUser();
        followFacade.follow(user.getId(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable("userId") Long userId) {
        User user = currentUserDetailService.getFullCurrentUser();
        followFacade.unfollow(user.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/follow")
    public ResponseEntity<Map<String, Object>> getFollowStatus(@PathVariable("userId") Long userId) {
        User user = currentUserDetailService.getFullCurrentUser();
        boolean following = followFacade.isFollowing(user.getId(), userId);
        int followerCount = followFacade.getFollowerCount(userId);
        return ResponseEntity.ok(Map.of("following", following, "followerCount", followerCount));
    }

    @GetMapping("/users/me/following")
    public ResponseEntity<List<UserDto>> getMyFollowing() {
        User user = currentUserDetailService.getFullCurrentUser();
        Collection<UserFollow> following = followFacade.getFollowing(user.getId());
        List<UserDto> dtos = following.stream()
                .map(f -> userMapper.toDto(f.getFollowed()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/users/me/followers")
    public ResponseEntity<Map<String, Object>> getMyFollowers() {
        User user = currentUserDetailService.getFullCurrentUser();
        Collection<UserFollow> followers = followFacade.getFollowers(user.getId());
        List<UserDto> dtos = followers.stream()
                .map(f -> userMapper.toDto(f.getFollower()))
                .toList();
        return ResponseEntity.ok(Map.of(
                "followers", dtos,
                "count", followers.size()
        ));
    }
}
