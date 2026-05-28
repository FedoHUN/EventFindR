package sk.eventfindr.fsa.domain;

public class EventArtist {

    private Long id;
    private Long eventId;
    private Long artistUserId;
    private String artistName;
    private int sortOrder;

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

    public Long getArtistUserId() {
        return artistUserId;
    }

    public void setArtistUserId(Long artistUserId) {
        this.artistUserId = artistUserId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
