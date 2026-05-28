package sk.eventfindr.fsa.jpa;

import jakarta.persistence.AttributeConverter;
import sk.eventfindr.fsa.domain.NotificationType;

public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType value) {
        return value == null ? null : value.name();
    }

    @Override
    public NotificationType convertToEntityAttribute(String value) {
        return value == null ? null : NotificationType.valueOf(value);
    }
}
