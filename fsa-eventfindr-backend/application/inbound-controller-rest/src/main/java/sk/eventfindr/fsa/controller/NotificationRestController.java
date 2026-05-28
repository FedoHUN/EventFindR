package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.NotificationFacade;
import sk.eventfindr.fsa.mapper.EventMapper;
import sk.eventfindr.fsa.rest.dto.NotificationDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class NotificationRestController {

    private final NotificationFacade notificationFacade;
    private final EventMapper eventMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public NotificationRestController(NotificationFacade notificationFacade,
                                      EventMapper eventMapper,
                                      CurrentUserDetailService currentUserDetailService) {
        this.notificationFacade = notificationFacade;
        this.eventMapper = eventMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        User user = currentUserDetailService.getFullCurrentUser();
        Collection<Notification> notifications = notificationFacade.getNotifications(user.getId());
        return ResponseEntity.ok(eventMapper.toNotificationDtoList(notifications));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        User user = currentUserDetailService.getFullCurrentUser();
        int count = notificationFacade.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        User user = currentUserDetailService.getFullCurrentUser();
        notificationFacade.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        User user = currentUserDetailService.getFullCurrentUser();
        notificationFacade.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
