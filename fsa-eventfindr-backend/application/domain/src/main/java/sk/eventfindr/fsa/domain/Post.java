package sk.eventfindr.fsa.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {

    private Long id;
    private String content;
    private Date created;
    private Date updated;
    private User author;
    private List<PostMedia> media = new ArrayList<>();
    private int mediaCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<PostMedia> getMedia() {
        return media;
    }

    public void setMedia(List<PostMedia> media) {
        this.media = media;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public void validateDeletion(User actor) {
        if (actor == null) {
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "An authenticated user is required");
        }

        boolean isAuthor = author != null
                && author.getId() != null
                && author.getId().equals(actor.getId());
        if (!isAuthor && actor.getRole() != UserRole.ADMIN) {
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only the post author or an admin can delete a post");
        }
    }
}
