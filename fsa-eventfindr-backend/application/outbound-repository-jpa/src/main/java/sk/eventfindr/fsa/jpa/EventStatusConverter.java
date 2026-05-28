package sk.eventfindr.fsa.jpa;

import jakarta.persistence.AttributeConverter;
import sk.eventfindr.fsa.domain.EventStatus;

public class EventStatusConverter implements AttributeConverter<EventStatus, String> {

    @Override
    public String convertToDatabaseColumn(EventStatus value) {
        return value == null ? null : value.name();
    }

    @Override
    public EventStatus convertToEntityAttribute(String value) {
        return value == null ? null : EventStatus.valueOf(value);
    }
}
