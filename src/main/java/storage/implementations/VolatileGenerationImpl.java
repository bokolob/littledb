package storage.implementations;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import row.Key;
import row.Value;
import storage.VolatileGeneration;

public class VolatileGenerationImpl implements VolatileGeneration {
    private ConcurrentSkipListMap<Key, Value> storage;

    private AtomicBoolean synced;

    public VolatileGenerationImpl() {
        storage = new ConcurrentSkipListMap<>();
        synced = new AtomicBoolean(false);
    }

    @Override
    public Optional<Value> get(Key key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public boolean isSynced() {
        return synced.get();
    }

    public boolean trySetSyncFlag() {
        return !synced.compareAndExchange(false, true);
    }

    @Override
    public void set(Key key, Value value) {
        storage.put(key, value);
    }

    public long size() {
        return storage.size();
    }

    @Override
    public Iterator<Map.Entry<Key, Value>> iterator() {
        return storage.entrySet().iterator();
    }
}
