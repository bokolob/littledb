package row;

import java.nio.ByteBuffer;

public interface Value {
    Timestamp getTimeStamp();
    byte[] asBytes();
    ByteBuffer asByteBuffer();
    int size();
}
