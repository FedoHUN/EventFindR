package sk.eventfindr.fsa.domain;

import java.math.BigDecimal;
import java.util.Date;

public class Event {

    private Long id;
    private String name;
    private String description;
    private String location;
    private Date eventDate;
    private BigDecimal price;
    private String ticketUrl;
    private String imageUrl;
    private String performers;
    private Date created;
    private User organizer;

    public Event() {
    }

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

    public String getPerformers() {
        return performers;
    }

    public void setPerformers(String performers) {
        this.performers = performers;
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

    public void prepareForCreation() {
        require(name != null && !name.isBlank(),
                EventfindrException.Type.VALIDATION,
                "Názov eventu je povinný údaj");
        require(location != null && !location.isBlank(),
                EventfindrException.Type.VALIDATION,
                "Miesto konania je povinný údaj");
        require(eventDate != null,
                EventfindrException.Type.VALIDATION,
                "Dátum konania je povinný údaj");
        require(organizer != null,
                EventfindrException.Type.VALIDATION,
                "Organizátor eventu je povinný údaj");

        name = name.trim();
        if (description != null) {
            description = description.trim();
        }
        location = location.trim();
        if (performers != null) {
            performers = performers.trim();
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Cena nemôže byť záporná");
        }
        if (created == null) {
            created = new Date();
        }
    }

    private void require(boolean valid, EventfindrException.Type type, String message) {
        if (!valid) {
            throw new EventfindrException(type, message);
        }
    }
}
