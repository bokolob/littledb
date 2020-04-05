package pages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import com.sun.nio.file.ExtendedOpenOption;

public class PageManagerImpl implements PageManager {
    public static final int PAGE_SIZE = 4096;
    public static final int PAGE_HDR_SIZE = 16;
    private final FileChannel randomAccessFile;
    private final Map<Long, Page> pageCache;
    private final int cacheSize;
    private Random random = new Random();

    private final int R_LOCK = 1;
    private final int W_LOCK = 2;

    public PageManagerImpl(Path path, int cacheSize) throws IOException {
        randomAccessFile = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ,
                ExtendedOpenOption.DIRECT);

        this.cacheSize = cacheSize;
        pageCache = new ConcurrentHashMap<>();
    }

    Page getPage(long pageNum, int lockMode) throws IOException {
        Page page;

        while (true) {
            page = pageCache.get(pageNum);

            if (page == null) {
                page = readPage(pageNum);
            }

            Lock lock;

            switch (lockMode) {
                case R_LOCK:
                    lock = page.getLock().readLock();
                    break;
                case W_LOCK:
                    lock = page.getLock().writeLock();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown lockMode " + lockMode);
            }

            lock.lock();

            if (!page.isEvicted()) {
                break;
            }

            lock.unlock();
        }

        return page;
    }

    private long realPageOffset(long pageNum) {
        //first page contains header
        return 1 + pageNum * (PAGE_HDR_SIZE + PAGE_SIZE);
    }

    private synchronized Page readPage(long pageNum) throws IOException {
        while (realPageOffset(pageNum) < randomAccessFile.size()) {
            allocPage(pageNum);
        }

        while (pageCache.size() >= cacheSize) {
            eviction();
        }

        long pos = randomAccessFile.position();
        randomAccessFile.position(realPageOffset(pageNum));

        randomAccessFile.position(pos + PAGE_HDR_SIZE);
        ByteBuffer buf = ByteBuffer.allocate(PAGE_SIZE);

        while (buf.position() < PAGE_SIZE) {
            int rc = randomAccessFile.read(buf);

            if (rc < 0 && buf.position() != PAGE_SIZE) {
                throw new IllegalArgumentException("File has been corrupted");
            }

        }

        buf.flip();

        return new PageImpl(buf);
    }

    //TODO LRU
    private void eviction() throws IOException {
        for (var n : pageCache.keySet()) {
            Page page = pageCache.get(n);
            page.getLock().writeLock().lock();
            if (!page.isEvicted() && random.nextDouble() < 0.3) {
                try {
                    syncPage(n, page);
                    page.setEvicted();
                    pageCache.remove(n);
                } finally {
                    page.getLock().writeLock().unlock();
                }
            }
        }
    }

    private void allocPage(long pageNumber) throws IOException {
        syncPage(pageNumber, new PageImpl(ByteBuffer.wrap(new byte[PAGE_SIZE])));
    }

    @Override
    public void read(byte[] dst, long fileOffset, int length) throws IOException {
        long pageNum = fileOffset / PAGE_SIZE;
        int pageOffset = (int) (fileOffset % PAGE_SIZE);
        Page page = getPage(pageNum, R_LOCK);
        page.read(pageOffset, dst, length);
        page.getLock().readLock().unlock();
    }

    @Override
    public void write(byte[] src, long fileOffset, int length) throws IOException {
        long pageNum = fileOffset / PAGE_SIZE;
        int pageOffset = (int) (fileOffset % PAGE_SIZE);
        Page page = getPage(pageNum, W_LOCK);
        page.setDirty(true);
        page.write(pageOffset, src, length);
        page.getLock().writeLock().unlock();
    }

    @Override
    public void sync() throws IOException {
        for (var pageNumber : pageCache.keySet()) {
            Page page = pageCache.get(pageNumber);

            if (page == null) {
                continue;
            }

            page.getLock().writeLock().lock();
            if (!page.isEvicted()) {
                try {
                    syncPage(pageNumber, page);
                } finally {
                    page.getLock().writeLock().unlock();
                }
            }
        }
    }

    private void syncPage(long pageNumber, Page page) throws IOException {
        if (!page.isDirty()) {
            return;
        }

        randomAccessFile.position(realPageOffset(pageNumber));
        //TODO header
        randomAccessFile.position(realPageOffset(pageNumber) + PAGE_HDR_SIZE);
        page.data().limit(PAGE_SIZE);
        page.data().position(0);
        randomAccessFile.write(page.data());
        page.setDirty(false);
    }

    @Override
    public void close() throws IOException { //disable all IO operations
        sync();
        randomAccessFile.close();
    }

}
