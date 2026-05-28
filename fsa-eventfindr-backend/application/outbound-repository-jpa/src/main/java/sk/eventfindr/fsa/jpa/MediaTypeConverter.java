package sk.eventfindr.fsa.jpa;

import jakarta.persistence.AttributeConverter;
import sk.eventfindr.fsa.domain.MediaType;

public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @Override
    public MediaType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        try {
            return MediaType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid value for enum MediaType: " + value);
        }
    }
}
