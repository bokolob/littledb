package storage.implementations.disk.persistence;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import row.Key;
import row.Value;
import row.ValueImpl;
import storage.implementations.disk.DataStreamOutput;
import storage.implementations.disk.IndexStreamInput;
import storage.implementations.disk.IndexStreamOutput;
import storage.implementations.disk.PersistentGenerationMergingIterator;
import storage.implementations.disk.PrimaryIndex;
import storage.implementations.disk.PrimaryIndexImpl;
import storage.implementations.disk.persistence.messages.AddGenerationRequest;
import storage.implementations.disk.persistence.messages.AddGenerationResponse;
import storage.implementations.disk.persistence.messages.FileLookupRequest;
import storage.implementations.disk.persistence.messages.FileLookupResponse;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class FileService extends BaseActorImpl {
    private final String root;
    private final SortedSet<FileEntry> fileEntries;
    private final AtomicBoolean mergeInProgress;
    private final ActorMessageRouter router;


    public FileService(int threadCount, ActorMessageRouter router, String root) {
        super(threadCount, router);
        this.root = root;
        this.fileEntries = new ConcurrentSkipListSet<>(new FileEntryComparator().reversed());
        mergeInProgress = new AtomicBoolean(false);

        this.registerMessageHandler(FileLookupRequest.class, this::search);
        this.registerMessageHandler(AddGenerationRequest.class, this::addGeneration);

        this.router = router;
    }

    @Override
    public void run() {
        try {
            init();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        super.run();
    }

    public void init() throws FileNotFoundException {
        File folder = new File(root);
        for (var file : Objects.requireNonNull(folder.listFiles((d, f) -> f.endsWith(FileEntry.INDEX_FILE_EXT)))) {
            fileEntries.add(new FileEntry(file));
        }
    }

    public void search(FileLookupRequest key) {
        for (var entry : fileEntries) {

            Value f;
            try {
                f = searchInOneEntry(entry, key.getRequestObject());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if (f != null) {
                router.sendResponse(new FileLookupResponse(key, f));
                return;
            }
        }

        router.sendResponse(new FileLookupResponse(key, null));

    }

    public void addGeneration(AddGenerationRequest volatileGeneration) {
        try {
            fileEntries.add(FileEntry.fromIterator(root, volatileGeneration.getRequestObject().iterator()));
            router.sendResponse(new AddGenerationResponse(volatileGeneration, null));
            mergePersistentTables();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Value searchInOneEntry(FileEntry fileEntry, Key key) throws IOException {
        fileEntry.read();
        PrimaryIndex.IndexEntry indexEntry = fileEntry.index.search(key);

        if (indexEntry != null) {
            return fileEntry.readValueByIndex(indexEntry);
        }

        return null;
    }

    private void mergePersistentTables() throws Exception {
        if (!mergeInProgress.compareAndSet(false, true)) {
            return;
        }

        if (fileEntries.size() < 6) {
            mergeInProgress.set(false);
            return;
        }

        System.err.println("Merging " + fileEntries.size());

        Iterator<FileEntry> fileEntryIterator = fileEntries.iterator();
        List<FileEntry> toRemove = new ArrayList<>();
        List<FileEntry> toAdd = new ArrayList<>();

        while (fileEntryIterator.hasNext()) {
            FileEntry f1 = fileEntryIterator.next();

            if (!fileEntryIterator.hasNext()) {
                break;
            }

            FileEntry f2 = fileEntryIterator.next();

            PersistentGenerationMergingIterator merger =
                    new PersistentGenerationMergingIterator(new FileEntryIterator(f1), new FileEntryIterator(f2));

            FileEntry mergedEntry = FileEntry.fromIterator(root, merger);

            toAdd.add(mergedEntry);

            toRemove.add(f1);
            toRemove.add(f2);
        }

        for (int i = 0; i < toAdd.size(); i++) {
            fileEntries.add(toAdd.get(i));
            fileEntries.remove(toRemove.get(2 * i));
            fileEntries.remove(toRemove.get(2 * i + 1));

            toRemove.get(2 * i).remove();
            toRemove.get(2 * i + 1).remove();
        }

        mergeInProgress.set(false);
    }

    private static class FileEntryComparator implements Comparator<FileEntry> {
        @Override
        public int compare(FileEntry o1, FileEntry o2) {
            return o1.indexFile.getName().compareTo(o2.indexFile.getName());
        }
    }

    public static class FileEntryIterator implements Iterator<Map.Entry<Key, Value>> {
        private final FileEntry fileEntry;
        private Iterator<PrimaryIndex.IndexEntry> indexIterator;

        public FileEntryIterator(FileEntry fileEntry) throws IOException {
            this.fileEntry = fileEntry;
            indexIterator = fileEntry.getIndex().iterator();
        }

        @Override
        public boolean hasNext() {
            return indexIterator.hasNext();
        }

        @Override
        public Map.Entry<Key, Value> next() {
            PrimaryIndex.IndexEntry nextEntry = indexIterator.next();
            Value v = fileEntry.readValueByIndex(nextEntry);

            if (v == null) {
                throw new IllegalStateException();
            }

            return new AbstractMap.SimpleEntry<>(nextEntry.getKey(), v);
        }
    }

    public static class FileEntry {
        private static final String INDEX_FILE_EXT = ".idx";
        private static final String DATA_FILE_EXT = ".dat";

        private final File indexFile;
        private volatile PrimaryIndex index;
        private volatile RandomAccessFile dataFile;
        private volatile boolean isRemoved = false;
        private final ReentrantLock lock = new ReentrantLock();

        public void remove() throws IOException {
            lock.lock();
            try {
                dataFile.close();
                Files.delete(Path.of(indexFile.getAbsolutePath()));
                Files.delete(Path.of(getDataPath()));
            } finally {
                isRemoved = true;
                lock.unlock();
            }
        }

        public FileEntry(File indexFile) throws FileNotFoundException {
            this(indexFile, null);
        }

        public FileEntry(File indexFile, PrimaryIndex index) throws FileNotFoundException {
            this.indexFile = indexFile;
            this.index = index;
            this.dataFile = new RandomAccessFile(getDataPath(), "r");
        }

        private String getDataPath() {
            return indexFile.getAbsolutePath().substring(0,
                    indexFile.getAbsolutePath().length() - INDEX_FILE_EXT.length())
                    + DATA_FILE_EXT;
        }

        public void read() throws IOException {
            if (index != null || isRemoved) {
                return;
            }

            lock.lock();

            try {
                if (index != null || isRemoved) {
                    return;
                }
                System.err.println("Load " + indexFile);
                index = PrimaryIndexImpl
                        .fromInputStream(new IndexStreamInput(new FileInputStream(indexFile)));


            } catch (EOFException e) {
                throw new IOException(e.getMessage() + ", incorrect file " + indexFile);
            } finally {
                lock.unlock();
            }
        }

        public static FileEntry fromIterator(String root, Iterator<Map.Entry<Key, Value>> iterator) throws Exception {
            String path = root + "/" + System.currentTimeMillis();

            String indexPath = path + INDEX_FILE_EXT;
            String dataPath = path + DATA_FILE_EXT;

            PrimaryIndexImpl.Builder indexBuilder = new PrimaryIndexImpl.Builder();

            try (IndexStreamOutput indexWriter = new IndexStreamOutput(new FileOutputStream(indexPath + ".tmp"));
                 DataStreamOutput dataWriter = new DataStreamOutput(new FileOutputStream(dataPath + ".tmp"))
            ) {
                while (iterator.hasNext()) {
                    Map.Entry<Key, Value> entry = iterator.next();
                    PrimaryIndex.IndexEntry indexEntry = indexWriter.writeRecord(entry.getKey(), entry.getValue());
                    dataWriter.writeRecord(entry.getValue());

                    indexBuilder.addEntry(indexEntry);
                }
            }

            Files.move(Path.of(dataPath + ".tmp"), Path.of(dataPath), ATOMIC_MOVE);
            Files.move(Path.of(indexPath + ".tmp"), Path.of(indexPath), ATOMIC_MOVE);

            return new FileEntry(Path.of(indexPath).toFile(), indexBuilder.build());
        }

        public Value readValueByIndex(PrimaryIndex.IndexEntry entry) {

            byte[] buffer = new byte[entry.getLength()];
            try {
                lock.lock();
                read();

                if (isRemoved) {
                    return null;
                }

                dataFile.seek(entry.getOffset());
                dataFile.read(buffer, 0, entry.getLength());
                return new ValueImpl(buffer, entry.getTimestamp());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FileEntry fileEntry = (FileEntry) o;
            return Objects.equals(indexFile, fileEntry.indexFile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(indexFile);
        }

        public PrimaryIndex getIndex() throws IOException {
            read();
            return index;
        }
    }
}