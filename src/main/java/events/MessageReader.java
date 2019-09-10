package events;

import java.nio.ByteBuffer;
import java.util.Iterator;

public interface MessageReader extends Iterator<Message> {
    void consume(ByteBuffer incoming);
}
