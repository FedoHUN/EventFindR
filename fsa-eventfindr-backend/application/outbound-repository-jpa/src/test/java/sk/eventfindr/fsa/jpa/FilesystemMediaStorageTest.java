package sk.eventfindr.fsa.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesystemMediaStorageTest {

    @TempDir
    private Path tempDir;

    @Test
    void storesAndLoadsFileWhenBasePathIsRelative() {
        Path relativeStorage = Path.of("target", "test-media-storage", UUID.randomUUID().toString());
        FilesystemMediaStorage storage = new FilesystemMediaStorage(relativeStorage.toString());
        storage.init();

        byte[] data = "image".getBytes();

        storage.store(data, "stored.jpg");

        assertArrayEquals(data, storage.load("stored.jpg"));
        assertTrue(Files.exists(relativeStorage.toAbsolutePath().normalize().resolve("stored.jpg")));
    }

    @Test
    void rejectsPathTraversal() {
        FilesystemMediaStorage storage = new FilesystemMediaStorage(tempDir.resolve("media-storage").toString());
        storage.init();

        assertThrows(IllegalArgumentException.class, () -> storage.store(new byte[]{1}, "../outside.jpg"));
    }
}
