package sk.eventfindr.fsa.jpa;

import jakarta.persistence.AttributeConverter;
import sk.eventfindr.fsa.domain.UserRole;

public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole code) {
        if (code == null) {
            return null;
        }
        return code.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        try {
            return UserRole.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid value for enum UserRole: " + code);
        }
    }
}
