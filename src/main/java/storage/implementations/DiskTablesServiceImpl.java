package storage.implementations;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import row.Key;
import row.Value;
import storage.DataStreamOutput;
import storage.DiskTablesService;
import storage.GenerationInterface;
import storage.IndexStreamInput;
import storage.IndexStreamOutput;
import storage.PersistentGeneration;
import storage.PrimaryIndex;
import storage.VolatileGeneration;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class DiskTablesServiceImpl implements DiskTablesService {
    private static final String DATA_PATH = "/Users/oxid/development/.littledb";
    private static final long MAX_IN_MEMORY_DB_SIZE = 1000L;
    private static final String INDEX_FILE_EXT = ".idx";
    private static final String DATA_FILE_EXT = ".dat";
    private final File folder = new File(DATA_PATH);
    volatile boolean in = false;

    private VolatileGenerationHolder volatileGenerationHolder;
    private GenerationInterface lastSyncedGeneration;

    private TimedCache<PersistentGeneration> fileCache;
    private Supplier<VolatileGeneration> volatileGenerationSupplier;
    private ExecutorService syncExecutorService;
    private ExecutorService fileReadingService;

    private class DaemonFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }

    public DiskTablesServiceImpl(Supplier<VolatileGeneration> volatileGenerationSupplier) {
        if (!folder.exists()) {
            folder.mkdir();
        }

        ThreadFactory threadFactory = new DaemonFactory();

        syncExecutorService = Executors.newSingleThreadExecutor(threadFactory);
        fileReadingService = Executors.newSingleThreadExecutor(threadFactory);

        this.fileCache = new TimedCache<>();
        this.volatileGenerationSupplier = volatileGenerationSupplier;
        this.volatileGenerationHolder = new VolatileGenerationHolder(this.volatileGenerationSupplier.get());
        this.lastSyncedGeneration = null;
    }

    private void mergePersistentTables() throws Exception {

        if (in) {
            throw new IllegalStateException("FUCK!");
        }

        in = true;

        File[] files = Objects.requireNonNull(folder.listFiles());
        Arrays.sort(files);

        List<String> indexFiles = Stream.of(files).sorted()
                .map(File::getAbsolutePath)
                .filter(e -> e.endsWith(INDEX_FILE_EXT))
                .collect(Collectors.toList());

        if (indexFiles.size() < 6) {
            in = false;
            return;
        }

        for (int i = 0; i + 1 < indexFiles.size(); i += 2) {
            PersistentGeneration p1 = readFromDisk(indexFiles.get(i));
            PersistentGeneration p2 = readFromDisk(indexFiles.get(i + 1));

            PersistentGenerationMergingIterator merger =
                    new PersistentGenerationMergingIterator(p1.iterator(), p2.iterator());

            //System.out.println(Thread.currentThread()+" start merging "+indexFiles.get(i)+" and "+ indexFiles.get
            // (i+1));
            saveToDisk(merger);

            //TODO список файлов может быть прочитан в другом месте но чуть раньше и без нового файла, зато со
            // старыми, которые мы удаляем
            //И удаление из кеша сломаное - объект может кто-то использовать.
            Files.delete(Path.of(indexFiles.get(i)));
            Files.delete(Path.of(getDataPath(indexFiles.get(i))));
            fileCache.delete(indexFiles.get(i));

            Files.delete(Path.of(indexFiles.get(i + 1)));
            Files.delete(Path.of(getDataPath(indexFiles.get(i + 1))));
            fileCache.delete(indexFiles.get(i + 1));
        }

        in = false;
        //System.out.println("Merging finished");
    }

    private String getDataPath(String indexPath) {
        return indexPath.substring(0, indexPath.length() - INDEX_FILE_EXT.length())
                + DATA_FILE_EXT;
    }

    private PersistentGeneration readFromDisk(String indexPath) throws IOException {

        PersistentGeneration cachedRef = fileCache.get(indexPath);

        if (cachedRef != null) {
            return cachedRef;
        }

        try {
            System.err.println("Load " + indexPath);
            PrimaryIndex index = PrimaryIndexImpl
                    .fromInputStream(new IndexStreamInput(new FileInputStream(indexPath)));

            PersistentGeneration generation = new PersistentGenerationImpl(index, getDataPath(indexPath), fileReadingService);

            System.out.println("Add: " + indexPath);
            fileCache.add(indexPath, generation, 15000L + System.currentTimeMillis());

            return generation;

        } catch (EOFException e) {
            throw new IOException(e.getMessage() + ", incorrect file " + indexPath);
        }
    }

    private PersistentGeneration saveToDisk(Iterator<Map.Entry<Key, Value>> iterator) throws Exception {
        String path = DATA_PATH + "/" + System.currentTimeMillis();

        String indexPath = path + INDEX_FILE_EXT;
        String dataPath = path + DATA_FILE_EXT;

        PrimaryIndexImpl.Builder indexBuilder = new PrimaryIndexImpl.Builder();

        try (IndexStreamOutput indexWriter = new IndexStreamOutput(new FileOutputStream(indexPath + ".tmp"));
             DataStreamOutput dataWriter = new DataStreamOutput(new FileOutputStream(dataPath + ".tmp"))
        )
        {
            while (iterator.hasNext()) {
                Map.Entry<Key, Value> entry = iterator.next();
                PrimaryIndex.IndexEntry indexEntry = indexWriter.writeRecord(entry.getKey(), entry.getValue());
                dataWriter.writeRecord(entry.getValue());

                indexBuilder.addEntry(indexEntry);
            }
        }

        PersistentGeneration persistentGeneration =
                new PersistentGenerationImpl(indexBuilder.build(), dataPath + ".tmp", fileReadingService);
        fileCache.add(indexPath, persistentGeneration, 30000 + System.currentTimeMillis());

        Files.move(Path.of(dataPath + ".tmp"), Path.of(dataPath), ATOMIC_MOVE);
        Files.move(Path.of(indexPath + ".tmp"), Path.of(indexPath), ATOMIC_MOVE);

        return persistentGeneration;
    }

    @Override
    public Optional<Value> lookupInMemory(Key key) {
        VolatileGeneration storage = volatileGenerationHolder.getVolatileGeneration();
        try {
            Optional<Value> found = storage.get(key);

            if (found.isEmpty() && lastSyncedGeneration != null) {
                found = lastSyncedGeneration.get(key);
            }

            return found;
        } finally {
            volatileGenerationHolder.releaseVolatileGeneration();
        }
    }

    @Override
    public void set(Key key, Value value) {
        VolatileGeneration storage = volatileGenerationHolder.getVolatileGeneration();

        try {
            storage.set(key, value);
        } finally {
            volatileGenerationHolder.releaseVolatileGeneration();
        }

        if (storage.size() >= MAX_IN_MEMORY_DB_SIZE && storage.trySetSyncFlag()) {
            syncExecutorService.submit(() -> saveGeneration(storage));
        }
    }

    private void saveGeneration(VolatileGeneration reference) {
        try {
            saveToDisk(reference.iterator());
        } catch (Exception e) {
            //TODO logging
            e.printStackTrace();
        }

        lastSyncedGeneration = reference;

        if (!volatileGenerationHolder.replaceVolatileGeneration(reference, volatileGenerationSupplier.get())) {
            throw new IllegalStateException("Somebody replaced volatileGenerationReference!");
        }

        try {
            //  System.err.println("merge!");
            mergePersistentTables();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Optional<Value> lookupOnDisk(Key key) throws IOException {
        File[] files = Objects.requireNonNull(folder.listFiles());
        Arrays.sort(files);

        for (int i = files.length - 1; i >= 0; i--) {
            File file = files[i];

            if (file.getName().endsWith(INDEX_FILE_EXT)) {
                String indexPath = file.getAbsolutePath();

                PersistentGeneration persistentGeneration = readFromDisk(indexPath);
                Optional<Value> value = persistentGeneration.get(key);

                if (value.isPresent()) {
                    fileCache.prolongate(indexPath, 10000);
                    return value;
                }

            }
        }

        return Optional.empty();
    }

    private static class VolatileGenerationHolder {
        private AtomicStampedReference<VolatileGeneration> volatileGenerationRef;

        public VolatileGenerationHolder(VolatileGeneration firstGeneration)
        {
            this.volatileGenerationRef = new AtomicStampedReference<>(firstGeneration, 0);
        }

        private VolatileGeneration changeCountVolatileGeneration(int delta) {

            VolatileGeneration expected = volatileGenerationRef.getReference();
            int count = volatileGenerationRef.getStamp();

            while (!volatileGenerationRef.compareAndSet(expected, expected, count, count + delta)) {
                expected = volatileGenerationRef.getReference();
                count = volatileGenerationRef.getStamp();
            }

            return expected;
        }

        private VolatileGeneration getVolatileGeneration() {
            return changeCountVolatileGeneration(1);
        }

        private void releaseVolatileGeneration() {
            changeCountVolatileGeneration(-1);
        }

        private boolean replaceVolatileGeneration(VolatileGeneration oldOne, VolatileGeneration newOne) {
            VolatileGeneration expected = volatileGenerationRef.getReference();

            while (expected == oldOne && !volatileGenerationRef.compareAndSet(expected, newOne, 0, 0)) {
                expected = volatileGenerationRef.getReference();
            }

            return expected == oldOne;
        }
    }
}
