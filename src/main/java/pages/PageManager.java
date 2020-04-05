package pages;

import java.io.IOException;

public interface PageManager {
    void read(byte[] dst, long fileOffset, int length) throws IOException;

    void write(byte[] src, long fileOffset, int length) throws IOException;

    void sync() throws IOException;

    void close() throws IOException;
}
