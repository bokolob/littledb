package storage.implementations.disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import row.Key;
import row.Value;
import row.ValueImpl;

public class PersistentGenerationImpl implements PersistentGeneration {
    private PrimaryIndex index;
    private RandomAccessFile dataFile;
    private ExecutorService fileReaderThread;

    public PersistentGenerationImpl(PrimaryIndex primaryIndex, String path, ExecutorService fileReadingService)
            throws IOException
    {
        this.index = primaryIndex;
        this.dataFile = new RandomAccessFile(path, "r");
        this.fileReaderThread = fileReadingService;
    }

    public PrimaryIndex.IndexEntry search(Key key) {
        return index.search(key);
    }

    @Override
    public CompletableFuture<Value> getAsync(PrimaryIndex.IndexEntry indexEntry) {
        return CompletableFuture.supplyAsync(() -> readValueByIndex(indexEntry), fileReaderThread);
    }

    @Override
    public Optional<Value> get(Key key) {
        return readWithWait(index.search(key));
    }

    private Optional<Value> readWithWait(PrimaryIndex.IndexEntry entry) {
        if (entry != null) {
            Future<Value> value = getAsync(entry);
            try {
                return Optional.of(value.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    private Value readValueByIndex(PrimaryIndex.IndexEntry entry) {
        byte[] buffer = new byte[entry.getLength()];
        try {
            dataFile.seek(entry.getOffset());
            dataFile.read(buffer, 0, entry.getLength());
            return new ValueImpl(buffer, entry.getTimestamp());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isSynced() {
        return false;
    }

    @Override
    public long size() {
        return index.size();
    }

    @Override
    public Iterator<Map.Entry<Key, Value>> iterator() {
        return new PersistentGenerationIterator(this);
    }

    @Override
    public void close() throws Exception {
        dataFile.close();
    }

    @Override
    public int compareTo(PersistentGeneration o) {
        return 0;
    }

    public static class PersistentGenerationIterator implements Iterator<Map.Entry<Key, Value>> {
        private PersistentGenerationImpl persistentGeneration;
        private Iterator<PrimaryIndex.IndexEntry> indexIterator;

        public PersistentGenerationIterator(PersistentGenerationImpl persistentGeneration) {
            this.persistentGeneration = persistentGeneration;
            indexIterator = persistentGeneration.index.iterator();
        }

        @Override
        public boolean hasNext() {
            return indexIterator.hasNext();
        }

        @Override
        public Map.Entry<Key, Value> next() {
            PrimaryIndex.IndexEntry nextEntry = indexIterator.next();
            return new AbstractMap.SimpleEntry<>(nextEntry.getKey(),
                    persistentGeneration.readWithWait(nextEntry).get());
        }
    }
}
