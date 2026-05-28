package sk.eventfindr.fsa.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class Event {

    private Long id;
    private String name;
    private String description;
    private String location;
    private Date eventDate;
    private BigDecimal price;
    private String ticketUrl;
    private String imageUrl;
    private String genre;
    private EventStatus status;
    private boolean featured;
    private List<EventArtist> artists;
    private boolean canceled;
    private Date created;
    private User organizer;
    private Integer capacity;
    private int attendingCount;
    private int watchingCount;
    private int commentCount;
    private Double averageRating;
    private int ratingCount;
    private Long coverImageMediaId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public List<EventArtist> getArtists() {
        return artists;
    }

    public void setArtists(List<EventArtist> artists) {
        this.artists = artists;
    }

    public int getAttendingCount() {
        return attendingCount;
    }

    public void setAttendingCount(int attendingCount) {
        this.attendingCount = attendingCount;
    }

    public int getWatchingCount() {
        return watchingCount;
    }

    public void setWatchingCount(int watchingCount) {
        this.watchingCount = watchingCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Long getCoverImageMediaId() {
        return coverImageMediaId;
    }

    public void setCoverImageMediaId(Long coverImageMediaId) {
        this.coverImageMediaId = coverImageMediaId;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public void cancel(User actor) {
        validateActorCanManage(actor, "Only the organizer or an admin can cancel an event");
        canceled = true;
    }

    public void restore(User actor) {
        validateActorCanManage(actor, "Only the organizer or an admin can restore an event");
        canceled = false;
    }

    public void publish(User actor) {
        validateActorCanManage(actor, "Only the organizer or an admin can publish an event");
        status = EventStatus.PUBLISHED;
    }

    public void toggleFeatured(User actor) {
        require(actor != null, EventfindrException.Type.FORBIDDEN, "An authenticated user is required");
        require(actor.getRole() == UserRole.ADMIN,
                EventfindrException.Type.FORBIDDEN,
                "Only an admin can mark an event as featured");
        featured = !featured;
    }

    public boolean isOwnedBy(User user) {
        return user != null
                && organizer != null
                && organizer.getId() != null
                && organizer.getId().equals(user.getId());
    }

    public void validateForUpdate(User actor) {
        validateActorCanManage(actor, "Only the organizer or an admin can update an event");
    }

    public void prepareForCreation() {
        prepareForCreation(Instant.now());
    }

    public void prepareForCreation(Instant now) {
        validate();
        if (created == null) {
            created = Date.from(now);
        }
    }

    public void validate() {
        require(name != null && !name.isBlank(),
                EventfindrException.Type.VALIDATION,
                "Event name is required");
        require(location != null && !location.isBlank(),
                EventfindrException.Type.VALIDATION,
                "Event location is required");
        require(eventDate != null,
                EventfindrException.Type.VALIDATION,
                "Event date is required");
        require(organizer != null,
                EventfindrException.Type.VALIDATION,
                "Event organizer is required");

        name = name.trim();
        if (description != null) {
            description = description.trim();
        }
        location = location.trim();

        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event price cannot be negative");
        }
        if (capacity != null && capacity < 1) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Event capacity must be at least 1");
        }
        if (status == null) {
            status = EventStatus.PUBLISHED;
        }
    }

    private void validateActorCanManage(User actor, String message) {
        require(actor != null, EventfindrException.Type.FORBIDDEN, "An authenticated user is required");
        require(isOwnedBy(actor) || actor.getRole() == UserRole.ADMIN,
                EventfindrException.Type.FORBIDDEN,
                message);
    }

    private void require(boolean valid, EventfindrException.Type type, String message) {
        if (!valid) {
            throw new EventfindrException(type, message);
        }
    }
}
