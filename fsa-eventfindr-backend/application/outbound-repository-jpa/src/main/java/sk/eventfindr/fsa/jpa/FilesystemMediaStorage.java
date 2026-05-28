package sk.eventfindr.fsa.jpa;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import sk.eventfindr.fsa.domain.MediaStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class FilesystemMediaStorage implements MediaStorage {

    private final Path basePath;

    public FilesystemMediaStorage(@Value("${eventfindr.media.storage-path:./media-storage}") String storagePath) {
        this.basePath = Path.of(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create media storage directory: " + basePath, e);
        }
    }

    @Override
    public void store(byte[] data, String fileName) {
        try {
            Files.write(safePath(fileName), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public byte[] load(String fileName) {
        try {
            return Files.readAllBytes(safePath(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load file: " + fileName, e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(safePath(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + fileName, e);
        }
    }

    private Path safePath(String fileName) {
        Path resolved = basePath.resolve(fileName).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Path traversal detected: " + fileName);
        }
        return resolved;
    }
}
