package storage;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

public interface AsyncWriteCallback {
    void onValueBlockRead(Integer size, ByteBuffer buffer) throws ClosedChannelException;
}
