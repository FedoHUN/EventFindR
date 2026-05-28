package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Notification;

import java.util.Collection;

public interface NotificationFacade {

    Collection<Notification> getNotifications(Long userId);

    int getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void createEventReminders();

    void notifyFollowersOfNewEvent(Long organizerId, Long eventId, String eventName);

    void notifyAttendeesOfCancellation(Long eventId, String eventName);

    void notifyAttendeesOfUpdate(Long eventId, String eventName);

    void notifyEventOrganizerOfComment(Long eventId, Long organizerId, String commenterName);

    void notifyUserOfNewFollower(Long followedId, String followerName);
}
