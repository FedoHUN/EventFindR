package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.Event;

import java.util.Collection;

public interface EventDiscoveryFacade {

    Collection<Event> findByArtistUserId(Long artistUserId);

    Collection<Event> findDraftsByOrganizer(Long organizerId);

    Collection<Event> findSimilar(Long eventId);

    Collection<Event> findTrending(int limit);
}
