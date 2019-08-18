package storage;

import java.io.IOException;

import row.Key;
import row.Value;

public interface StorageService {
    //Optional<Value> get(Key key) throws IOException;
    void get(Key key, AsyncWriteCallback callback) throws IOException;
    void set(Key key, Value value) throws IOException;
}
