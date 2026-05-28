package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventTest {

    @Test
    void prepareForCreationSetsCreatedAndTrimsFields() {
        Event event = new Event();
        event.setName("  Pohoda Festival  ");
        event.setDescription("  Popis  ");
        event.setLocation("  Trencin  ");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());

        event.prepareForCreation();

        assertEquals("Pohoda Festival", event.getName());
        assertEquals("Popis", event.getDescription());
        assertEquals("Trencin", event.getLocation());
        assertNotNull(event.getCreated());
    }

    @Test
    void prepareForCreationKeepsExistingCreatedTimestamp() {
        Event event = new Event();
        Date created = new Date(1234L);
        event.setName("Test Event");
        event.setLocation("Bratislava");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());
        event.setCreated(created);

        event.prepareForCreation();

        assertEquals(created, event.getCreated());
    }

    @Test
    void prepareForCreationFailsWhenNameMissing() {
        Event event = new Event();
        event.setName("   ");
        event.setLocation("Bratislava");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Event name is required", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenLocationMissing() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation(null);
        event.setEventDate(new Date());
        event.setOrganizer(organizer());

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Event location is required", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenEventDateMissing() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Trencin");
        event.setEventDate(null);
        event.setOrganizer(organizer());

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Event date is required", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenOrganizerMissing() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Trencin");
        event.setEventDate(new Date());
        event.setOrganizer(null);

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Event organizer is required", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenPriceIsNegative() {
        Event event = validEvent();
        event.setPrice(BigDecimal.valueOf(-10));

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Event price cannot be negative", ex.getMessage());
    }

    @Test
    void prepareForCreationAcceptsZeroPrice() {
        Event event = validEvent();
        event.setPrice(BigDecimal.ZERO);

        event.prepareForCreation();

        assertEquals(BigDecimal.ZERO, event.getPrice());
    }

    @Test
    void prepareForCreationAcceptsNullPrice() {
        assertDoesNotThrow(() -> validEvent().prepareForCreation());
    }

    @Test
    void cancelAllowsOrganizer() {
        Event event = validEvent();

        event.cancel(organizer());

        assertEquals(true, event.isCanceled());
    }

    @Test
    void restoreAllowsAdmin() {
        Event event = validEvent();
        event.setCanceled(true);

        event.restore(user(99L, UserRole.ADMIN));

        assertEquals(false, event.isCanceled());
    }

    @Test
    void publishRejectsUnrelatedUser() {
        Event event = validEvent();

        EventfindrException ex = assertThrows(EventfindrException.class, () -> event.publish(user(42L, UserRole.USER)));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        assertEquals("Only the organizer or an admin can publish an event", ex.getMessage());
    }

    @Test
    void toggleFeaturedRequiresAdmin() {
        Event event = validEvent();

        EventfindrException ex = assertThrows(EventfindrException.class, () -> event.toggleFeatured(organizer()));

        assertEquals(EventfindrException.Type.FORBIDDEN, ex.getType());
        assertEquals("Only an admin can mark an event as featured", ex.getMessage());
    }

    @Test
    void isOwnedByMatchesOrganizerIdentity() {
        Event event = validEvent();

        assertEquals(true, event.isOwnedBy(organizer()));
    }

    private Event validEvent() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Bratislava");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());
        return event;
    }

    private User organizer() {
        return user(1L, UserRole.ORGANIZER);
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("user-" + id);
        user.setEmail("user" + id + "@eventfindr.sk");
        user.setRole(role);
        return user;
    }
}
