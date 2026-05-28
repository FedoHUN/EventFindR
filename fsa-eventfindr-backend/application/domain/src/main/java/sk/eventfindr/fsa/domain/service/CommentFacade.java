package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.EventComment;

import java.util.Collection;

public interface CommentFacade {

    EventComment addComment(Long eventId, Long userId, String content, Integer rating);

    Collection<EventComment> getComments(Long eventId);

    void deleteComment(Long commentId, Long userId);
}
