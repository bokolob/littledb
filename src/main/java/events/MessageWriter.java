package events;

import java.nio.ByteBuffer;

public interface MessageWriter {
    ByteBuffer[] asByteBufferSequence(Message message);
}
