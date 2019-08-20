package storage;

import java.util.Optional;

import row.Value;

public interface ValueSearchCallback {
    void onRead(Optional<Value> value);
}
