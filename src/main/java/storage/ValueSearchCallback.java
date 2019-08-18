package storage;

import java.nio.ByteBuffer;

public interface ValueSearchCallback {
    void onNotFound();
    void onValueFound(PrimaryIndex.IndexEntry entry);
    void onValueBlockRead(ByteBuffer buffer);
}
