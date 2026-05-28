package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.EventCommentRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;

import java.time.Clock;
import java.util.Collection;

public class CommentService implements CommentFacade {

    private final EventCommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationFacade notificationFacade;
    private final Clock clock;
    private final DomainLogger log;

    public CommentService(EventCommentRepository commentRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          NotificationFacade notificationFacade) {
        this(commentRepository, eventRepository, userRepository, notificationFacade,
                Clock.systemDefaultZone(), DomainLogger.noop());
    }

    public CommentService(EventCommentRepository commentRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          NotificationFacade notificationFacade,
                          Clock clock,
                          DomainLogger log) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationFacade = notificationFacade;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public EventComment addComment(Long eventId, Long userId, String content, Integer rating) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Event was not found"));

        User user = userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));

        EventComment comment = new EventComment();
        comment.setEventId(eventId);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        comment.prepareForCreation(clock.instant());
        commentRepository.save(comment);
        log.info("Added comment {} to event {} by user {}", comment.getId(), eventId, userId);

        Long organizerId = event.getOrganizer() != null ? event.getOrganizer().getId() : null;
        if (organizerId != null && !organizerId.equals(userId)) {
            notificationFacade.notifyEventOrganizerOfComment(eventId, organizerId, user.getName());
        }

        return comment;
    }

    @Override
    public Collection<EventComment> getComments(Long eventId) {
        return commentRepository.findByEventId(eventId);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        EventComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "Comment was not found"));

        User user = userRepository.get(userId)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User was not found"));

        comment.validateDeletion(user);
        commentRepository.delete(comment);
        log.info("Deleted comment {} by user {}", commentId, userId);
    }
}
