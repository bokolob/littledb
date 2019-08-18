package storage;

import row.Key;
import row.Value;

public interface VolatileGeneration extends GenerationInterface {
    void set(Key key, Value value);

    boolean trySetSyncFlag();
}
