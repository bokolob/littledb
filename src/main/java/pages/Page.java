package pages;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReadWriteLock;

public interface Page {

    int getCrc();

    ReadWriteLock getLock();

    boolean isDirty();

    void read(int from, byte[] dst, int length);

    void write(int to, byte[] src, int length);

    boolean isEvicted();

    void setDirty(boolean b);

    void setEvicted();

    ByteBuffer data();
}
