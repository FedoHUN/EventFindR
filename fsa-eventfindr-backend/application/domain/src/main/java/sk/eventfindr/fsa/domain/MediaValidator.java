package sk.eventfindr.fsa.domain;

import java.util.Objects;
import java.util.Set;

public final class MediaValidator {

    private static final long DEFAULT_MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long DEFAULT_MAX_VIDEO_SIZE = 75L * 1024 * 1024;

    private final Set<String> allowedImageTypes;
    private final Set<String> allowedVideoTypes;
    private final long maxImageSize;
    private final long maxVideoSize;

    public MediaValidator(Set<String> allowedImageTypes,
                          Set<String> allowedVideoTypes,
                          long maxImageSize,
                          long maxVideoSize) {
        this.allowedImageTypes = Set.copyOf(Objects.requireNonNull(allowedImageTypes));
        this.allowedVideoTypes = Set.copyOf(Objects.requireNonNull(allowedVideoTypes));
        this.maxImageSize = maxImageSize;
        this.maxVideoSize = maxVideoSize;
    }

    public static MediaValidator defaultValidator() {
        return new MediaValidator(
                Set.of("image/jpeg", "image/png", "image/webp"),
                Set.of("video/mp4", "video/quicktime"),
                DEFAULT_MAX_IMAGE_SIZE,
                DEFAULT_MAX_VIDEO_SIZE
        );
    }

    public MediaType resolveMediaType(String contentType) {
        if (allowedImageTypes.contains(contentType)) {
            return MediaType.IMAGE;
        }
        if (allowedVideoTypes.contains(contentType)) {
            return MediaType.VIDEO;
        }
        throw new EventfindrException(
                EventfindrException.Type.VALIDATION,
                "Unsupported file type: " + contentType + ". Allowed: JPEG, PNG, WebP, MP4, MOV."
        );
    }

    public void validateFileSize(MediaType mediaType, long fileSize) {
        if (fileSize <= 0) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Uploaded file cannot be empty."
            );
        }
        if (mediaType == MediaType.IMAGE && fileSize > maxImageSize) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Image is too large. Maximum is 5 MB."
            );
        }
        if (mediaType == MediaType.VIDEO && fileSize > maxVideoSize) {
            throw new EventfindrException(
                    EventfindrException.Type.VALIDATION,
                    "Video is too large. Maximum is 75 MB."
            );
        }
    }

    public boolean isSupportedContentType(String contentType) {
        return allowedImageTypes.contains(contentType) || allowedVideoTypes.contains(contentType);
    }

    public String extractExtension(String originalName, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            default -> "";
        };
    }
}
