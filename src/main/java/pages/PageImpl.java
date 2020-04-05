package pages;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PageImpl implements Page {
    private final ReentrantReadWriteLock lock;
    private int crc;
    private final ByteBuffer data;
    private volatile boolean isEvicted;
    private volatile boolean isDirty;

    public PageImpl(ByteBuffer data) {
        this.data = data;
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public int getCrc() {
        return crc;
    }

    @Override
    public ReadWriteLock getLock() {
        return lock;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void read(int from, byte[] dst, int length) {
        System.arraycopy(data.array(), from, dst, 0, length);
    }

    @Override
    public void write(int to, byte[] src, int length) {
        System.arraycopy(src, 0, data.array(), to, length);
    }

    @Override
    public boolean isEvicted() {
        return isEvicted;
    }

    @Override
    public void setDirty(boolean b) {
        isDirty = true;
    }

    @Override
    public void setEvicted() {
        isEvicted = true;
    }

    @Override
    public ByteBuffer data() {
        return null;
    }
}
