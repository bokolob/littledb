package storage.implementations;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import row.Key;
import row.Value;
import storage.DiskTablesService;
import storage.StorageService;

public class StorageServiceImpl implements StorageService {
    private Map<Key, ReadWriteLock> locked;
    private DiskTablesService diskTablesService;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public StorageServiceImpl(DiskTablesService diskTablesService) {
        this.diskTablesService = diskTablesService;
        locked = new ConcurrentSkipListMap<>();
    }

    private ReadWriteLock getLock(Key key) {
        return locked.computeIfAbsent(key, e -> new ReentrantReadWriteLock());
    }

    @Override
    public CompletableFuture<Optional<Value>> get(Key key) throws IOException {
        return getValue(key);

        /*return CompletableFuture.runAsync(() -> getLock(key).readLock().lock())
                .thenCompose(k -> {
                    try {
                        return getValue(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CompletableFuture.completedFuture(Optional.empty());
                })
                .thenApply(v -> {
                    getLock(key).readLock().unlock();
                    return v;
                })
                ; */
    }

    private CompletableFuture<Optional<Value>> getValue(Key key) throws IOException {

        Optional<Value> value = diskTablesService.lookupInMemory(key);

        if (value.isPresent()) {
            return CompletableFuture.completedFuture(value);
        }

        return diskTablesService
                .lookupOnDisk(key)
                .thenApply(v -> {
                    if (v.isPresent()) {
                        diskTablesService.set(key, v.get());
                        return v;
                    }

                    return Optional.empty();
                });
    }

    @Override
    public CompletableFuture<Void> set(Key key, Value value) throws IOException {

        return getValue(key).thenApply(v -> {
                    if(v.isPresent() && v.get().getTimeStamp().compareTo(value.getTimeStamp()) < 0)
                        diskTablesService.set(key, value);

                    return null;
                }
        );


       /* return CompletableFuture.runAsync(() -> getLock(key).writeLock().lock())
                .thenCompose(k -> {
                    try {
                        return getValue(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return CompletableFuture.completedFuture(Optional.empty());
                })
                .thenApply(
                        v -> {
                            if(v.isPresent() && v.get().getTimeStamp().compareTo(value.getTimeStamp()) < 0)
                                    diskTablesService.set(key, value);

                            return null;
                        }

                )
                .thenApply(v -> { getLock(key).writeLock().unlock(); return null;}); */
    }

}
