package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.EventMedia;

import java.util.Collection;
import java.util.List;

public interface EventMediaFacade {

    EventMedia uploadMedia(Long eventId, Long organizerId, String originalName,
                           String contentType, long fileSize, byte[] data);

    Collection<EventMedia> getMediaForEvent(Long eventId);

    byte[] getMediaFile(Long mediaId);

    void deleteMedia(Long mediaId, Long organizerId);

    void reorderMedia(Long eventId, Long organizerId, List<Long> mediaIds);
}
