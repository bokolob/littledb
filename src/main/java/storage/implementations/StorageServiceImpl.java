package storage.implementations;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import row.Key;
import row.Value;
import storage.DiskTablesService;
import storage.StorageService;

public class StorageServiceImpl implements StorageService {
    private DiskTablesService diskTablesService;

    public StorageServiceImpl(DiskTablesService diskTablesService) {
        this.diskTablesService = diskTablesService;
    }

    @Override
    public CompletableFuture<Optional<Value>> get(Key key) throws IOException {
        return getValue(key);
    }

    private CompletableFuture<Optional<Value>> getValue(Key key) throws IOException {

        Optional<Value> value = diskTablesService.lookupInMemory(key);

        if (value.isPresent()) {
            return CompletableFuture.completedFuture(value);
        }

        return diskTablesService
                .lookupOnDisk(key)
                .thenApply(v -> {
                    v.ifPresent(val -> diskTablesService.set(key, val));
                    return v;
                });
    }

    @Override
    public CompletableFuture<Void> set(Key key, Value value) throws IOException {

        return getValue(key).thenApply(v -> {
                    if(!v.isPresent() || v.get().getTimeStamp().compareTo(value.getTimeStamp()) < 0)
                        diskTablesService.set(key, value);

                    return null;
                }
        );

    }

}
