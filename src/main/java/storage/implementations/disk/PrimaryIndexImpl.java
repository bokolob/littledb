package storage.implementations.disk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import row.Key;

public class PrimaryIndexImpl implements PrimaryIndex {
    private List<Key> keys;
    private List<IndexEntry> entries; //sorted array

    private PrimaryIndexImpl(List<Key> keys, List<IndexEntry> entries) {
        this.keys = keys;
        this.entries = entries;
    }

    public static PrimaryIndexImpl fromInputStream(IndexStreamInput inputStream) throws IOException {
        PrimaryIndexImpl.Builder indexBuilder = new PrimaryIndexImpl.Builder();

        IndexEntry prev = null;

        while (inputStream.available() > 0) {
            IndexEntry current = inputStream.readRecord();

            if (prev != null) {

                if (current.getKey().compareTo(prev.getKey()) <= 0) {
                    throw new IllegalStateException("Corrupted index stream!");
                }

            }

            indexBuilder.addEntry(current);
            prev = current;
            //System.err.println("Loaded "+key+" "+new String(key.toBytes()));
        }

        return indexBuilder.build();
    }

    @Override
    public Iterator<IndexEntry> iterator() {
        return Collections.unmodifiableCollection(entries).iterator();
    }

    @Override
    public IndexEntry search(Key key) {
        int pos = Collections.binarySearch(keys, key);

        if (pos < 0) {
            return null;
        }

        return entries.get(pos);
    }

    @Override
    public long size() {
        return entries.size();
    }

    public static class Builder {
        private List<Key> keys;
        private List<IndexEntry> entries; //sorted array

        public Builder() {
            keys = new ArrayList<>();
            entries = new ArrayList<>();
        }

        public Builder addEntry(IndexEntry entry) {
            keys.add(entry.getKey());
            entries.add(entry);
            return this;
        }

        public PrimaryIndexImpl build() {
            PrimaryIndexImpl primaryIndex = new PrimaryIndexImpl(keys, entries);
            keys = null;
            entries = null;
            return primaryIndex;
        }
    }
}
