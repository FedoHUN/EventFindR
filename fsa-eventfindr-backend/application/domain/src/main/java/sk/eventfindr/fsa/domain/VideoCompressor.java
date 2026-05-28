package sk.eventfindr.fsa.domain;

public interface VideoCompressor {

    /**
     * Compresses the video file in-place, replacing the original with a smaller version.
     * If compression fails or FFmpeg is not available, the original file is kept unchanged.
     */
    void compress(String fileName);
}
