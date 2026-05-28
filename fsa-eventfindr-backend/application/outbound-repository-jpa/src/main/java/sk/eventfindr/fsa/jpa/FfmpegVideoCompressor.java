package sk.eventfindr.fsa.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sk.eventfindr.fsa.domain.VideoCompressor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class FfmpegVideoCompressor implements VideoCompressor {

    private static final Logger log = LoggerFactory.getLogger(FfmpegVideoCompressor.class);

    private final Path basePath;

    public FfmpegVideoCompressor(@Value("${eventfindr.media.storage-path:./media-storage}") String storagePath) {
        this.basePath = Path.of(storagePath).toAbsolutePath().normalize();
    }

    @Override
    @Async
    public void compress(String fileName) {
        Path input = resolveWithinStorage(fileName);
        Path output = resolveWithinStorage("compressed_" + fileName);

        try {
            long originalSize = Files.size(input);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", input.toString(),
                    "-vcodec", "libx264",
                    "-crf", "28",
                    "-preset", "fast",
                    "-acodec", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-y",
                    output.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            // Consume output to prevent blocking
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.warn("FFmpeg compression failed for {} with exit code {}", fileName, exitCode);
                Files.deleteIfExists(output);
                return;
            }

            long compressedSize = Files.size(output);

            if (compressedSize < originalSize) {
                Files.move(output, input, StandardCopyOption.REPLACE_EXISTING);
                log.info("Compressed video {}: {} MB -> {} MB",
                        fileName,
                        String.format("%.1f", originalSize / (1024.0 * 1024)),
                        String.format("%.1f", compressedSize / (1024.0 * 1024)));
            } else {
                log.info("Compressed file is not smaller for {}, keeping original", fileName);
                Files.deleteIfExists(output);
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Video compression unavailable for {}: {}", fileName, e.getMessage());
            try {
                Files.deleteIfExists(output);
            } catch (IOException ignored) {
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Path resolveWithinStorage(String fileName) {
        Path resolved = basePath.resolve(fileName).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid media file name");
        }
        return resolved;
    }
}
