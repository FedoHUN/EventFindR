package sk.eventfindr.fsa.domain;

public interface MediaStorage {

    void store(byte[] data, String fileName);

    byte[] load(String fileName);

    void delete(String fileName);
}
