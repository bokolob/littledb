package storage.implementations.disk;

import java.util.Map;
import java.util.Optional;

import row.Key;
import row.Value;

public interface GenerationInterface extends Iterable<Map.Entry<Key, Value>> {
    Optional<Value> get(Key key);

    boolean isSynced();

    long size();
}
