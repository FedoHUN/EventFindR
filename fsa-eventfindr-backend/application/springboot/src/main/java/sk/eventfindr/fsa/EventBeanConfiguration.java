package sk.eventfindr.fsa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.AttendanceStatus;
import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtistRepository;
import sk.eventfindr.fsa.domain.EventAttendance;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.EventCommentRepository;
import sk.eventfindr.fsa.domain.EventMedia;
import sk.eventfindr.fsa.domain.EventMediaRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.MediaStorage;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.NotificationRepository;
import sk.eventfindr.fsa.domain.PaginatedResult;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.PostMediaRepository;
import sk.eventfindr.fsa.domain.PostRepository;
import sk.eventfindr.fsa.domain.UserFollow;
import sk.eventfindr.fsa.domain.UserFollowRepository;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.VideoCompressor;
import sk.eventfindr.fsa.domain.service.CommentFacade;
import sk.eventfindr.fsa.domain.service.CommentService;
import sk.eventfindr.fsa.domain.service.EventAttendanceFacade;
import sk.eventfindr.fsa.domain.service.EventAttendanceService;
import sk.eventfindr.fsa.domain.service.EventDiscoveryFacade;
import sk.eventfindr.fsa.domain.service.EventDiscoveryService;
import sk.eventfindr.fsa.domain.service.EventEnrichmentService;
import sk.eventfindr.fsa.domain.service.EventFacade;
import sk.eventfindr.fsa.domain.service.EventMediaFacade;
import sk.eventfindr.fsa.domain.service.EventMediaService;
import sk.eventfindr.fsa.domain.service.EventRecommendationService;
import sk.eventfindr.fsa.domain.service.EventService;
import sk.eventfindr.fsa.domain.service.FollowFacade;
import sk.eventfindr.fsa.domain.service.FollowService;
import sk.eventfindr.fsa.domain.service.NotificationFacade;
import sk.eventfindr.fsa.domain.service.NotificationService;
import sk.eventfindr.fsa.domain.service.PostFacade;
import sk.eventfindr.fsa.domain.service.PostService;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class EventBeanConfiguration {

    @Bean
    public Clock domainClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public NotificationFacade notificationFacade(NotificationRepository notificationRepository,
                                                 EventRepository eventRepository,
                                                 EventAttendanceRepository attendanceRepository,
                                                 UserFollowRepository followRepository,
                                                 Clock domainClock) {
        NotificationFacade notificationService = new NotificationService(
                notificationRepository, eventRepository, attendanceRepository, followRepository,
                domainClock, logger(NotificationService.class));
        return new TransactionalNotificationFacade(notificationService);
    }

    @Bean
    public EventEnrichmentService eventEnrichmentService(EventArtistRepository eventArtistRepository,
                                                         EventAttendanceRepository eventAttendanceRepository,
                                                         EventCommentRepository eventCommentRepository,
                                                         EventMediaRepository eventMediaRepository) {
        return new EventEnrichmentService(
                eventArtistRepository, eventAttendanceRepository, eventCommentRepository, eventMediaRepository);
    }

    @Bean
    public EventMediaService eventMediaService(EventRepository eventRepository,
                                               EventMediaRepository eventMediaRepository,
                                               UserRepository userRepository,
                                               MediaStorage mediaStorage,
                                               VideoCompressor videoCompressor,
                                               Clock domainClock) {
        return new EventMediaService(
                eventRepository, eventMediaRepository, userRepository, mediaStorage, videoCompressor,
                sk.eventfindr.fsa.domain.MediaValidator.defaultValidator(),
                domainClock,
                logger(EventMediaService.class));
    }

    @Bean
    public EventRecommendationService eventRecommendationService(EventRepository eventRepository,
                                                                 EventEnrichmentService enrichmentService) {
        return new EventRecommendationService(eventRepository, enrichmentService);
    }

    @Bean
    public EventService eventServiceInstance(EventRepository eventRepository,
                                             UserRepository userRepository,
                                             EventMediaRepository eventMediaRepository,
                                             EventArtistRepository eventArtistRepository,
                                             NotificationFacade notificationFacade,
                                             EventEnrichmentService enrichmentService,
                                             EventMediaService mediaService,
                                             Clock domainClock) {
        return new EventService(
                eventRepository, userRepository, eventMediaRepository, eventArtistRepository,
                notificationFacade, enrichmentService, mediaService,
                domainClock, logger(EventService.class));
    }

    @Bean
    public EventAttendanceService eventAttendanceService(EventRepository eventRepository,
                                                        UserRepository userRepository,
                                                        EventAttendanceRepository eventAttendanceRepository,
                                                        Clock domainClock) {
        return new EventAttendanceService(
                eventRepository, userRepository, eventAttendanceRepository,
                domainClock, logger(EventAttendanceService.class));
    }

    @Bean
    public EventDiscoveryService eventDiscoveryService(EventRepository eventRepository,
                                                       EventArtistRepository eventArtistRepository,
                                                       EventEnrichmentService enrichmentService,
                                                       EventRecommendationService recommendationService) {
        return new EventDiscoveryService(
                eventRepository, eventArtistRepository, enrichmentService, recommendationService);
    }

    @Bean
    public EventFacade eventFacade(EventService eventService) {
        return new TransactionalEventFacade(eventService);
    }

    @Bean
    public EventAttendanceFacade eventAttendanceFacade(EventAttendanceService eventAttendanceService) {
        return new TransactionalEventAttendanceFacade(eventAttendanceService);
    }

    @Bean
    public EventDiscoveryFacade eventDiscoveryFacade(EventDiscoveryService eventDiscoveryService) {
        return new TransactionalEventDiscoveryFacade(eventDiscoveryService);
    }

    @Bean
    public EventMediaFacade eventMediaFacade(EventMediaService eventMediaService) {
        return new TransactionalEventMediaFacade(eventMediaService);
    }

    @Bean
    public CommentFacade commentFacade(EventCommentRepository commentRepository,
                                       EventRepository eventRepository,
                                       UserRepository userRepository,
                                       NotificationFacade notificationFacade,
                                       Clock domainClock) {
        CommentFacade commentService = new CommentService(
                commentRepository, eventRepository, userRepository, notificationFacade,
                domainClock, logger(CommentService.class));
        return new TransactionalCommentFacade(commentService);
    }

    @Bean
    public FollowFacade followFacade(UserFollowRepository followRepository,
                                     UserRepository userRepository,
                                     NotificationFacade notificationFacade,
                                     Clock domainClock) {
        FollowFacade followService = new FollowService(
                followRepository, userRepository, notificationFacade,
                domainClock, logger(FollowService.class));
        return new TransactionalFollowFacade(followService);
    }

    @Bean
    public PostFacade postFacade(PostRepository postRepository,
                                 PostMediaRepository postMediaRepository,
                                 UserRepository userRepository,
                                 MediaStorage mediaStorage,
                                 VideoCompressor videoCompressor,
                                 Clock domainClock) {
        PostFacade postService = new PostService(
                postRepository, postMediaRepository, userRepository, mediaStorage, videoCompressor,
                sk.eventfindr.fsa.domain.MediaValidator.defaultValidator(),
                domainClock,
                logger(PostService.class));
        return new TransactionalPostFacade(postService);
    }

    static DomainLogger logger(Class<?> type) {
        return new Slf4jDomainLogger(LoggerFactory.getLogger(type));
    }

    private record Slf4jDomainLogger(Logger logger) implements DomainLogger {
        @Override
        public void info(String message, Object... args) {
            logger.info(message, args);
        }

        @Override
        public void warn(String message, Object... args) {
            logger.warn(message, args);
        }
    }

    // --- Transactional wrappers ---

    @Transactional
    static class TransactionalEventFacade implements EventFacade {
        private final EventFacade delegate;
        TransactionalEventFacade(EventFacade delegate) { this.delegate = delegate; }

        @Override @Transactional(readOnly = true)
        public Collection<Event> readAll() { return delegate.readAll(); }
        @Override @Transactional(readOnly = true)
        public Collection<Event> readAllPublished() { return delegate.readAllPublished(); }
        @Override @Transactional(readOnly = true)
        public Event getById(Long id) { return delegate.getById(id); }
        @Override
        public Long create(Event event) { return delegate.create(event); }
        @Override
        public void update(Event event, Long userId) { delegate.update(event, userId); }
        @Override
        public void cancelEvent(Long eventId, Long userId) { delegate.cancelEvent(eventId, userId); }
        @Override
        public void restoreEvent(Long eventId, Long userId) { delegate.restoreEvent(eventId, userId); }
        @Override
        public void deleteEvent(Long eventId, Long userId) { delegate.deleteEvent(eventId, userId); }
        @Override
        public void publishEvent(Long eventId, Long userId) { delegate.publishEvent(eventId, userId); }
        @Override
        public void toggleFeatured(Long eventId, Long userId) { delegate.toggleFeatured(eventId, userId); }
    }

    @Transactional
    static class TransactionalEventAttendanceFacade implements EventAttendanceFacade {
        private final EventAttendanceFacade delegate;
        TransactionalEventAttendanceFacade(EventAttendanceFacade delegate) { this.delegate = delegate; }

        @Override
        public void attend(Long eventId, Long userId, String status) { delegate.attend(eventId, userId, status); }
        @Override
        public void unattend(Long eventId, Long userId) { delegate.unattend(eventId, userId); }
        @Override @Transactional(readOnly = true)
        public AttendanceStatus getAttendanceStatus(Long eventId, Long userId) { return delegate.getAttendanceStatus(eventId, userId); }
        @Override @Transactional(readOnly = true)
        public Collection<EventAttendance> getAttendancesByUser(Long userId) { return delegate.getAttendancesByUser(userId); }
        @Override @Transactional(readOnly = true)
        public Map<String, Integer> getAttendanceCounts(Long eventId) { return delegate.getAttendanceCounts(eventId); }
    }

    @Transactional
    static class TransactionalEventDiscoveryFacade implements EventDiscoveryFacade {
        private final EventDiscoveryFacade delegate;
        TransactionalEventDiscoveryFacade(EventDiscoveryFacade delegate) { this.delegate = delegate; }

        @Override @Transactional(readOnly = true)
        public Collection<Event> findByArtistUserId(Long artistUserId) { return delegate.findByArtistUserId(artistUserId); }
        @Override @Transactional(readOnly = true)
        public Collection<Event> findDraftsByOrganizer(Long organizerId) { return delegate.findDraftsByOrganizer(organizerId); }
        @Override @Transactional(readOnly = true)
        public Collection<Event> findSimilar(Long eventId) { return delegate.findSimilar(eventId); }
        @Override @Transactional(readOnly = true)
        public Collection<Event> findTrending(int limit) { return delegate.findTrending(limit); }
    }

    @Transactional
    static class TransactionalEventMediaFacade implements EventMediaFacade {
        private final EventMediaFacade delegate;
        TransactionalEventMediaFacade(EventMediaFacade delegate) { this.delegate = delegate; }

        @Override
        public EventMedia uploadMedia(Long eventId, Long organizerId, String originalName, String contentType, long fileSize, byte[] data) {
            return delegate.uploadMedia(eventId, organizerId, originalName, contentType, fileSize, data);
        }
        @Override @Transactional(readOnly = true)
        public Collection<EventMedia> getMediaForEvent(Long eventId) { return delegate.getMediaForEvent(eventId); }
        @Override @Transactional(readOnly = true)
        public byte[] getMediaFile(Long mediaId) { return delegate.getMediaFile(mediaId); }
        @Override
        public void deleteMedia(Long mediaId, Long organizerId) { delegate.deleteMedia(mediaId, organizerId); }
        @Override
        public void reorderMedia(Long eventId, Long organizerId, List<Long> mediaIds) { delegate.reorderMedia(eventId, organizerId, mediaIds); }
    }

    @Transactional
    static class TransactionalCommentFacade implements CommentFacade {
        private final CommentFacade delegate;
        TransactionalCommentFacade(CommentFacade delegate) { this.delegate = delegate; }

        @Override
        public EventComment addComment(Long eventId, Long userId, String content, Integer rating) { return delegate.addComment(eventId, userId, content, rating); }
        @Override @Transactional(readOnly = true)
        public Collection<EventComment> getComments(Long eventId) { return delegate.getComments(eventId); }
        @Override
        public void deleteComment(Long commentId, Long userId) { delegate.deleteComment(commentId, userId); }
    }

    @Transactional
    static class TransactionalFollowFacade implements FollowFacade {
        private final FollowFacade delegate;
        TransactionalFollowFacade(FollowFacade delegate) { this.delegate = delegate; }

        @Override
        public void follow(Long followerId, Long followedId) { delegate.follow(followerId, followedId); }
        @Override
        public void unfollow(Long followerId, Long followedId) { delegate.unfollow(followerId, followedId); }
        @Override @Transactional(readOnly = true)
        public boolean isFollowing(Long followerId, Long followedId) { return delegate.isFollowing(followerId, followedId); }
        @Override @Transactional(readOnly = true)
        public Collection<UserFollow> getFollowing(Long userId) { return delegate.getFollowing(userId); }
        @Override @Transactional(readOnly = true)
        public Collection<UserFollow> getFollowers(Long userId) { return delegate.getFollowers(userId); }
        @Override @Transactional(readOnly = true)
        public int getFollowerCount(Long userId) { return delegate.getFollowerCount(userId); }
    }

    @Transactional
    static class TransactionalNotificationFacade implements NotificationFacade {
        private final NotificationFacade delegate;
        TransactionalNotificationFacade(NotificationFacade delegate) { this.delegate = delegate; }

        @Override @Transactional(readOnly = true)
        public Collection<Notification> getNotifications(Long userId) { return delegate.getNotifications(userId); }
        @Override @Transactional(readOnly = true)
        public int getUnreadCount(Long userId) { return delegate.getUnreadCount(userId); }
        @Override
        public void markAsRead(Long notificationId, Long userId) { delegate.markAsRead(notificationId, userId); }
        @Override
        public void markAllAsRead(Long userId) { delegate.markAllAsRead(userId); }
        @Override
        public void createEventReminders() { delegate.createEventReminders(); }
        @Override
        public void notifyFollowersOfNewEvent(Long organizerId, Long eventId, String eventName) { delegate.notifyFollowersOfNewEvent(organizerId, eventId, eventName); }
        @Override
        public void notifyAttendeesOfCancellation(Long eventId, String eventName) { delegate.notifyAttendeesOfCancellation(eventId, eventName); }
        @Override
        public void notifyAttendeesOfUpdate(Long eventId, String eventName) { delegate.notifyAttendeesOfUpdate(eventId, eventName); }
        @Override
        public void notifyEventOrganizerOfComment(Long eventId, Long organizerId, String commenterName) { delegate.notifyEventOrganizerOfComment(eventId, organizerId, commenterName); }
        @Override
        public void notifyUserOfNewFollower(Long followedId, String followerName) { delegate.notifyUserOfNewFollower(followedId, followerName); }
    }

    @Transactional
    static class TransactionalPostFacade implements PostFacade {
        private final PostFacade delegate;
        TransactionalPostFacade(PostFacade delegate) { this.delegate = delegate; }

        @Override
        public Long createPost(Long authorId, String content) { return delegate.createPost(authorId, content); }
        @Override @Transactional(readOnly = true)
        public Collection<Post> getPostsByAuthor(Long authorId) { return delegate.getPostsByAuthor(authorId); }
        @Override @Transactional(readOnly = true)
        public PaginatedResult<Post> getPostsByAuthor(Long authorId, int page, int size) { return delegate.getPostsByAuthor(authorId, page, size); }
        @Override @Transactional(readOnly = true)
        public Post getPostById(Long postId) { return delegate.getPostById(postId); }
        @Override
        public void deletePost(Long postId, Long userId) { delegate.deletePost(postId, userId); }
        @Override
        public PostMedia uploadMedia(Long postId, Long userId, String originalName, String contentType, long fileSize, byte[] data) {
            return delegate.uploadMedia(postId, userId, originalName, contentType, fileSize, data);
        }
        @Override @Transactional(readOnly = true)
        public Collection<PostMedia> getMediaForPost(Long postId) { return delegate.getMediaForPost(postId); }
        @Override @Transactional(readOnly = true)
        public byte[] getMediaFile(Long mediaId) { return delegate.getMediaFile(mediaId); }
        @Override
        public void deleteMedia(Long mediaId, Long userId) { delegate.deleteMedia(mediaId, userId); }
    }
}
