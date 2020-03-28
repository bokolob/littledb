package storage.implementations.disk;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.function.Supplier;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import row.Key;
import row.Value;
import storage.implementations.disk.messages.lookup.LookupRequest;
import storage.implementations.disk.messages.lookup.LookupResponse;
import storage.implementations.disk.messages.set.SetKVRequest;
import storage.implementations.disk.messages.set.SetKVResponse;
import storage.implementations.disk.persistence.FileService;
import storage.implementations.disk.persistence.messages.AddGenerationRequest;
import storage.implementations.disk.persistence.messages.FileLookupRequest;

public class DiskTablesServiceImpl extends BaseActorImpl implements DiskTablesService {
    private final String dataPath;
    private static final long MAX_IN_MEMORY_DB_SIZE = 1000L;
    private static final String INDEX_FILE_EXT = ".idx";
    private static final String DATA_FILE_EXT = ".dat";
    private final File folder;
    volatile boolean in = false;

    private VolatileGenerationHolder volatileGenerationHolder;
    private GenerationInterface lastSyncedGeneration;

    private Supplier<VolatileGeneration> volatileGenerationSupplier;
    private final ActorMessageRouter router;
    private final FileService fileService;

    public DiskTablesServiceImpl(int threadCount,
                                 String dataRoot,
                                 Supplier<VolatileGeneration> volatileGenerationSupplier,
                                 ActorMessageRouter router) {
        super(threadCount, router);
        this.router = router;

        this.registerMessageHandler(LookupRequest.class, this::lookup);
        this.registerMessageHandler(SetKVRequest.class, this::set);

        dataPath = dataRoot;
        folder = new File(dataPath);

        if (!folder.exists()) {
            folder.mkdir();
        }

        this.volatileGenerationSupplier = volatileGenerationSupplier;
        this.volatileGenerationHolder = new VolatileGenerationHolder(this.volatileGenerationSupplier.get());
        this.lastSyncedGeneration = null;

        fileService = new FileService(threadCount, router, dataRoot);
        fileService.run();
    }


    void lookup(LookupRequest request) {

        Optional<Value> value = lookupInMemory(request);

        if (value.isPresent()) {
            router.sendResponse(new LookupResponse(request, value.get()));
            return;
        }

        lookupOnDisk(request);
    }

    Optional<Value> lookupInMemory(LookupRequest request) {
        VolatileGeneration storage = volatileGenerationHolder.getVolatileGeneration();

        try {
            Optional<Value> found = storage.get(request.getRequestObject());

            if (found.isEmpty() && lastSyncedGeneration != null) {
                found = lastSyncedGeneration.get(request.getRequestObject());
            }

            return found;
        } finally {
            volatileGenerationHolder.releaseVolatileGeneration();
        }
    }

    void set(SetKVRequest request) {
        set(request.getRequestObject().getKey(), request.getRequestObject().getValue());
        router.sendResponse(new SetKVResponse(request, null));
    }

    void set(Key key, Value value) {
        VolatileGeneration storage = volatileGenerationHolder.getVolatileGeneration();

        try {
            storage.set(key, value);
        } finally {
            volatileGenerationHolder.releaseVolatileGeneration();
        }

        if (storage.size() >= MAX_IN_MEMORY_DB_SIZE) {
            saveGeneration(storage);
        }
    }

    private void saveGeneration(VolatileGeneration reference) { //TODO lock ?
        if (lastSyncedGeneration != null && !lastSyncedGeneration.isSynced()) {
            return;
        }

        if (!volatileGenerationHolder.replaceVolatileGeneration(reference, volatileGenerationSupplier.get())) {
            return;
        }

        lastSyncedGeneration = reference;
        fileService.pushMessage(new AddGenerationRequest(this, reference, e -> reference.trySetSyncFlag()));
    }


    void lookupOnDisk(LookupRequest diskLookupRequest) {
        fileService.pushMessage(new FileLookupRequest(this,
                diskLookupRequest.getRequestObject(),
                e -> {
                    e.ifPresent(value -> set(diskLookupRequest.getRequestObject(), value));
                    router.sendResponse(new LookupResponse(diskLookupRequest, e.orElse(null)));
                }
        ));
    }

    private static class VolatileGenerationHolder {
        private AtomicStampedReference<VolatileGeneration> volatileGenerationRef;

        public VolatileGenerationHolder(VolatileGeneration firstGeneration) {
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
