package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventTest {

    @Test
    void prepareForCreationSetsCreatedAndTrimsFields() {
        Event event = new Event();
        event.setName("  Pohoda Festival  ");
        event.setDescription("  Popis  ");
        event.setLocation("  Trenčín  ");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());

        event.prepareForCreation();

        assertEquals("Pohoda Festival", event.getName());
        assertEquals("Popis", event.getDescription());
        assertEquals("Trenčín", event.getLocation());
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
        assertEquals("Názov eventu je povinný údaj", ex.getMessage());
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
        assertEquals("Miesto konania je povinný údaj", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenEventDateMissing() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Trenčín");
        event.setEventDate(null);
        event.setOrganizer(organizer());

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Dátum konania je povinný údaj", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenOrganizerMissing() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Trenčín");
        event.setEventDate(new Date());
        event.setOrganizer(null);

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Organizátor eventu je povinný údaj", ex.getMessage());
    }

    @Test
    void prepareForCreationFailsWhenPriceIsNegative() {
        Event event = new Event();
        event.setName("Pohoda");
        event.setLocation("Trenčín");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());
        event.setPrice(BigDecimal.valueOf(-10));

        EventfindrException ex = assertThrows(EventfindrException.class, event::prepareForCreation);

        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
        assertEquals("Cena nemôže byť záporná", ex.getMessage());
    }

    @Test
    void prepareForCreationAcceptsZeroPrice() {
        Event event = new Event();
        event.setName("Free Event");
        event.setLocation("Bratislava");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());
        event.setPrice(BigDecimal.ZERO);

        event.prepareForCreation();

        assertEquals(BigDecimal.ZERO, event.getPrice());
    }

    @Test
    void prepareForCreationAcceptsNullPrice() {
        Event event = new Event();
        event.setName("Free Event");
        event.setLocation("Bratislava");
        event.setEventDate(new Date());
        event.setOrganizer(organizer());
        event.setPrice(null);

        event.prepareForCreation();

    }

    private User organizer() {
        User user = new User();
        user.setId(1L);
        user.setName("Organizer");
        user.setEmail("org@eventfindr.sk");
        user.setRola(UserRole.ORGANIZER);
        return user;
    }
}
