package sk.eventfindr.fsa.domain.service;

import sk.eventfindr.fsa.domain.DomainLogger;
import sk.eventfindr.fsa.domain.EventfindrException;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Collection;
import java.util.Optional;

public class UserService implements UserFacade {

    private static final int MAX_PROFILE_DESCRIPTION_LENGTH = 2000;

    private final UserRepository userRepository;
    private final DomainLogger log;

    public UserService(UserRepository userRepository) {
        this(userRepository, DomainLogger.noop());
    }

    public UserService(UserRepository userRepository, DomainLogger log) {
        this.userRepository = userRepository;
        this.log = log == null ? DomainLogger.noop() : log;
    }

    @Override
    public Optional<User> get(long id) {
        return userRepository.get(id);
    }

    @Override
    public Optional<User> get(String email) {
        return userRepository.get(email);
    }

    @Override
    public Collection<User> getOrganizers() {
        return userRepository.findByRole(UserRole.ORGANIZER);
    }

    @Override
    public void create(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Rejected user creation because email was missing");
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "User email is required");
        }
        if (userRepository.get(user.getEmail()).isPresent()) {
            log.warn("Rejected user creation because email {} already exists", user.getEmail());
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "A user with this email already exists");
        }
        userRepository.create(user);
        log.info("Created user account for {}", user.getEmail());
    }

    @Override
    public void becomeOrganizer(String email, String organizationName) {
        User user = getRequiredUser(email);

        if (user.getRole() == UserRole.ORGANIZER || user.getRole() == UserRole.ADMIN) {
            log.warn("Rejected organizer upgrade for {} because role is already {}", email, user.getRole());
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "User already has role " + user.getRole().name());
        }

        user.setRole(UserRole.ORGANIZER);
        user.setOrganizationName(requireText(organizationName, "Organization name is required"));
        userRepository.update(user);
        log.info("User {} became organizer", email);
    }

    @Override
    public void becomeArtist(String email, String artistName) {
        User user = getRequiredUser(email);

        if (user.getArtistName() != null && !user.getArtistName().isBlank()) {
            log.warn("Rejected artist upgrade for {} because artist profile already exists", email);
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "User is already registered as an artist");
        }

        if (user.getRole() == UserRole.ADMIN) {
            log.warn("Rejected artist upgrade for {} because admins cannot change role", email);
            throw new EventfindrException(
                    EventfindrException.Type.CONFLICT,
                    "Admin users cannot change role");
        }

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ARTIST);
        }
        user.setArtistName(requireText(artistName, "Artist name is required"));
        userRepository.update(user);
        log.info("User {} became artist", email);
    }

    @Override
    public void updateOrganizationName(String email, String organizationName) {
        User user = getRequiredUser(email);
        requireOrganizerPrivileges(user, "Only organizers or admins can update the organization name");

        user.setOrganizationName(requireText(organizationName, "Organization name is required"));
        userRepository.update(user);
        log.info("Updated organization name for {}", email);
    }

    @Override
    public void updateOrganizationDescription(String email, String description) {
        User user = getRequiredUser(email);
        requireOrganizerPrivileges(user, "Only organizers or admins can update the organization description");

        user.setOrganizationDescription(normalizeDescription(description, "Organization description cannot exceed 2000 characters"));
        userRepository.update(user);
        log.info("Updated organization description for {}", email);
    }

    @Override
    public void updateArtistDescription(String email, String description) {
        User user = getRequiredUser(email);
        if (user.getArtistName() == null || user.getArtistName().isBlank()) {
            log.warn("Rejected artist description update for {} because user is not an artist", email);
            throw new EventfindrException(
                    EventfindrException.Type.FORBIDDEN,
                    "Only artists can update the artist description");
        }

        user.setArtistDescription(normalizeDescription(description, "Artist description cannot exceed 2000 characters"));
        userRepository.update(user);
        log.info("Updated artist description for {}", email);
    }

    @Override
    public Collection<User> searchArtists(String nameFragment) {
        if (nameFragment == null || nameFragment.isBlank()) {
            return userRepository.findAllArtists();
        }
        return userRepository.searchArtistsByName(nameFragment.trim());
    }

    private User getRequiredUser(String email) {
        return userRepository.get(email)
                .orElseThrow(() -> new EventfindrException(
                        EventfindrException.Type.NOT_FOUND,
                        "User with email " + email + " was not found"));
    }

    private void requireOrganizerPrivileges(User user, String message) {
        if (user.getRole() != UserRole.ORGANIZER && user.getRole() != UserRole.ADMIN) {
            log.warn("Rejected organizer-only action for {} with role {}", user.getEmail(), user.getRole());
            throw new EventfindrException(EventfindrException.Type.FORBIDDEN, message);
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new EventfindrException(EventfindrException.Type.VALIDATION, message);
        }
        return value.trim();
    }

    private String normalizeDescription(String description, String message) {
        if (description != null && description.length() > MAX_PROFILE_DESCRIPTION_LENGTH) {
            throw new EventfindrException(EventfindrException.Type.VALIDATION, message);
        }
        return description == null ? null : description.trim();
    }
}
