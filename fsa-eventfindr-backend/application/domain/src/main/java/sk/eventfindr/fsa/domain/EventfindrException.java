package sk.eventfindr.fsa.domain;

import java.util.List;

public class EventfindrException extends RuntimeException {

    public enum Type {
        VALIDATION,
        NOT_FOUND,
        UNAUTHORIZED,
        FORBIDDEN,
        CONFLICT
    }

    private final Type type;
    private final List<String> details;

    public EventfindrException(Type type, String message) {
        super(message);
        this.type = type == null ? Type.VALIDATION : type;
        this.details = List.of();
    }

    public EventfindrException(Type type, String message, List<String> details) {
        super(message);
        this.type = type == null ? Type.VALIDATION : type;
        this.details = details == null ? List.of() : details;
    }

    public EventfindrException(Type type, String message, Throwable cause, List<String> details) {
        super(message, cause);
        this.type = type == null ? Type.VALIDATION : type;
        this.details = details == null ? List.of() : details;
    }

    public EventfindrException(String message) {
        this(Type.VALIDATION, message);
    }

    public EventfindrException(String message, Throwable cause) {
        this(Type.VALIDATION, message, cause, List.of());
    }

    public Type getType() {
        return type;
    }

    public List<String> getDetails() {
        return details;
    }
}
