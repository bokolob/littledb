package storage;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import row.Key;
import row.Value;

public interface DiskTablesService {
    Optional<Value> lookupInMemory(Key key);

    void set(Key key, Value value);

    CompletableFuture<Optional<Value>> lookupOnDisk(Key key) throws IOException;
}
