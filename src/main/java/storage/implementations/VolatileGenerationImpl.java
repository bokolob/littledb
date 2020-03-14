package storage.implementations;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import row.Key;
import row.Value;
import storage.implementations.disk.VolatileGeneration;

public class VolatileGenerationImpl implements VolatileGeneration {
    private ConcurrentSkipListMap<Key, AtomicReference<Value>> storage;

    private AtomicBoolean synced;

    public VolatileGenerationImpl() {
        storage = new ConcurrentSkipListMap<>();
        synced = new AtomicBoolean(false);
    }

    @Override
    public Optional<Value> get(Key key) {
        AtomicReference<Value> reference = storage.get(key);
        return Optional.ofNullable(reference == null ? null : reference.get());
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

        AtomicReference<Value> reference = storage.computeIfAbsent(key, k -> new AtomicReference<>(value));

        if (reference.get() == value) {
            return;
        }

        Value currentValue = reference.get();

        while (currentValue.getTimeStamp().compareTo(value.getTimeStamp()) < 0) {
            reference.compareAndSet(currentValue, value);
            currentValue = reference.get();
        }

    }

    public long size() {
        return storage.size();
    }

    public  static class Entry implements Map.Entry<Key, Value> {

        private Key key;
        private Value value;

        public Entry(Key key, Value value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Key getKey() {
            return key;
        }

        @Override
        public Value getValue() {
            return value;
        }

        @Override
        public Value setValue(Value value) {
            this.value = value;
            return value;
        }
    }

    public static class VolatileGenerationIterator implements Iterator<Map.Entry<Key, Value>> {

        private Iterator<Map.Entry<Key, AtomicReference<Value>>> iterator;

        public VolatileGenerationIterator(
                Iterator<Map.Entry<Key, AtomicReference<Value>>> iterator)
        {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map.Entry<Key, Value> next() {
            Map.Entry<Key, AtomicReference<Value>> val = iterator.next();

            return new Entry(val.getKey(), val.getValue().get());
        }
    }

    @Override
    public Iterator<Map.Entry<Key, Value>> iterator() {
        return new VolatileGenerationIterator(storage.entrySet().iterator());
    }
}
