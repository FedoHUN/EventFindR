package sk.eventfindr.fsa.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaValidatorTest {

    private final MediaValidator validator = MediaValidator.defaultValidator();

    @Test
    void resolveMediaTypeDetectsImage() {
        assertEquals(MediaType.IMAGE, validator.resolveMediaType("image/jpeg"));
    }

    @Test
    void resolveMediaTypeRejectsUnsupportedType() {
        EventfindrException ex = assertThrows(EventfindrException.class, () -> validator.resolveMediaType("text/html"));
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void validateFileSizeRejectsOversizedImage() {
        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> validator.validateFileSize(MediaType.IMAGE, 6L * 1024 * 1024));
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void validateFileSizeRejectsEmptyFile() {
        EventfindrException ex = assertThrows(EventfindrException.class,
                () -> validator.validateFileSize(MediaType.IMAGE, 0));
        assertEquals(EventfindrException.Type.VALIDATION, ex.getType());
    }

    @Test
    void extractExtensionFallsBackToContentType() {
        assertEquals(".mp4", validator.extractExtension(null, "video/mp4"));
    }

    @Test
    void extractExtensionIgnoresUntrustedOriginalFilename() {
        assertEquals(".jpg", validator.extractExtension("photo.jpg/../../evil.html", "image/jpeg"));
    }

    @Test
    void supportedContentTypeRecognizesWebp() {
        assertTrue(validator.isSupportedContentType("image/webp"));
    }
}
