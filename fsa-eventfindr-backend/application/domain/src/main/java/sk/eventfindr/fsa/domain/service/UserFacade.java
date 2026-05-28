package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.User;

import java.util.Collection;
import java.util.Optional;

public interface UserFacade {

    Optional<User> get(long id);

    Optional<User> get(String email);

    Collection<User> getOrganizers();

    void create(User user);

    void becomeOrganizer(String email, String organizationName);

    void becomeArtist(String email, String artistName);

    void updateOrganizationName(String email, String organizationName);

    void updateOrganizationDescription(String email, String description);

    void updateArtistDescription(String email, String description);

    Collection<User> searchArtists(String nameFragment);
}
