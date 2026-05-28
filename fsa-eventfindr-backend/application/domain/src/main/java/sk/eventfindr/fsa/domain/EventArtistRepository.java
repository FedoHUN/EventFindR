package sk.eventfindr.fsa.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EventArtistRepository {

    Collection<EventArtist> findByEventId(Long eventId);

    Map<Long, List<EventArtist>> findByEventIds(Collection<Long> eventIds);

    Collection<EventArtist> findByArtistUserId(Long artistUserId);

    void save(EventArtist eventArtist);

    void deleteByEventId(Long eventId);
}
