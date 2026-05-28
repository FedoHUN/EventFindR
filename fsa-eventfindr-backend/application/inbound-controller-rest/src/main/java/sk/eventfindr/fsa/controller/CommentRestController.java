package sk.eventfindr.fsa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.service.CommentFacade;
import sk.eventfindr.fsa.mapper.EventMapper;
import sk.eventfindr.fsa.rest.dto.EventCommentDto;
import sk.eventfindr.fsa.security.CurrentUserDetailService;

import java.util.Collection;
import java.util.List;

@RestController
public class CommentRestController {

    private final CommentFacade commentFacade;
    private final EventMapper eventMapper;
    private final CurrentUserDetailService currentUserDetailService;

    public CommentRestController(CommentFacade commentFacade,
                                 EventMapper eventMapper,
                                 CurrentUserDetailService currentUserDetailService) {
        this.commentFacade = commentFacade;
        this.eventMapper = eventMapper;
        this.currentUserDetailService = currentUserDetailService;
    }

    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<EventCommentDto>> getComments(@PathVariable("eventId") Long eventId) {
        Collection<EventComment> comments = commentFacade.getComments(eventId);
        return ResponseEntity.ok(eventMapper.toCommentDtoList(comments));
    }

    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<EventCommentDto> addComment(@PathVariable("eventId") Long eventId,
                                                      @RequestBody AddCommentRequest request) {
        User user = currentUserDetailService.getFullCurrentUser();
        EventComment comment = commentFacade.addComment(eventId, user.getId(), request.content(), request.rating());
        return ResponseEntity.status(201).body(eventMapper.toCommentDto(comment));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        User user = currentUserDetailService.getFullCurrentUser();
        commentFacade.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    record AddCommentRequest(String content, Integer rating) {}
}
