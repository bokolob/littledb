package storage;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import row.Key;
import row.Value;

public interface StorageService {
    CompletableFuture<Optional<Value>> get(Key key) throws IOException;
    CompletableFuture<Void> set(Key key, Value value) throws IOException;
}
