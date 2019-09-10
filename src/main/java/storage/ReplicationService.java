package storage;

import row.Key;
import row.Value;

public interface ReplicationService {
    void replicate(Key key, Value value);
}
