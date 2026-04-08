package sk.eventfindr.fsa.jpa;

import jakarta.persistence.AttributeConverter;
import sk.eventfindr.fsa.domain.AttendanceStatus;

public class AttendanceStatusConverter implements AttributeConverter<AttendanceStatus, String> {

    @Override
    public String convertToDatabaseColumn(AttendanceStatus code) {
        if (code == null) {
            return null;
        }
        return code.name();
    }

    @Override
    public AttendanceStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        try {
            return AttendanceStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid value for enum AttendanceStatus: " + code);
        }
    }
}
