package sk.eventfindr.fsa.domain;

import java.time.Instant;
import java.util.Date;

public class EventComment {

    private Long id;
    private Long eventId;
    private User user;
    private String content;
    private Integer rating;
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void prepareForCreation() {
        prepareForCreation(Instant.now());
    }

    public void prepareForCreation(Instant now) {
        if (content == null || content.isBlank()) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Comment content cannot be empty");
        }
        content = content.trim();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Rating must be between 1 and 5");
        }
        if (created == null) {
            created = Date.from(now);
        }
    }

    public void validateDeletion(User actor) {
        if (actor == null) {
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "An authenticated user is required");
        }

        boolean isAuthor = user != null
                && user.getId() != null
                && user.getId().equals(actor.getId());
        if (!isAuthor && actor.getRole() != UserRole.ADMIN) {
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only the comment author or an admin can delete a comment");
        }
    }
}
