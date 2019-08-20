package storage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import row.Key;
import row.Value;

public interface PersistentGeneration extends GenerationInterface, Comparable<PersistentGeneration>, AutoCloseable {
    PrimaryIndex.IndexEntry search(Key key);
    CompletableFuture<Value> getAsync(PrimaryIndex.IndexEntry indexEntry);
}
