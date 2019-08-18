package storage.implementations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import row.Key;
import row.Value;
import storage.DiskTablesService;
import storage.AsyncWriteCallback;
import storage.StorageService;

public class StorageServiceImpl implements StorageService {

    Map<Key, ReadWriteLock> locked;
    private DiskTablesService diskTablesService;
    private volatile boolean needSync;

    public StorageServiceImpl(DiskTablesService diskTablesService) {
        this.diskTablesService = diskTablesService;
        locked = new ConcurrentSkipListMap<>();
    }

    private ReadWriteLock getLock(Key key) {
        return locked.computeIfAbsent(key, e -> new ReentrantReadWriteLock());
    }

    @Override
    public void get(Key key, AsyncWriteCallback callback) throws IOException {
        getLock(key).readLock().lock();

        try {
            Optional<Value> value = getValue(key);

            if (value.isPresent()) {
                callback.onValueBlockRead(value.get().size(), ByteBuffer.wrap(value.get().asBytes()));
            }

            callback.onValueBlockRead(-1, null);
        } finally {
            getLock(key).readLock().unlock();
        }
    }

    private Optional<Value> getValue(Key key) throws IOException {

        Optional<Value> value = diskTablesService.lookupInMemory(key);

        if (value.isPresent()) {
            return value;
        }

        value = diskTablesService.lookupOnDisk(key);

        if (value.isPresent()) {
            diskTablesService.set(key, value.get());
            return value;
        }

        return Optional.empty();
    }

    @Override
    public void set(Key key, Value value) throws IOException {
        getLock(key).writeLock().lock();

        try {
            Value oldValue = getValue(key).orElse(null);

            if (oldValue == null || oldValue.getTimeStamp().compareTo(value.getTimeStamp()) < 0) {
                diskTablesService.set(key, value);
            }

            //System.err.println(Thread.currentThread()+" Create "+key+ " "+value);

        } finally {
            getLock(key).writeLock().unlock();
        }

    }

}
