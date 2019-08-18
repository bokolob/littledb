package row;

public class ValueImpl implements Value {
    final byte[] bytes;
    final Timestamp timestamp;

    @Override
    public String toString() {
        return new String(bytes);
    }

    public ValueImpl(String str, Timestamp timestamp) {
        this(str.getBytes(), timestamp);
    }

    public ValueImpl(byte[] buffer, Timestamp timestamp) {
        this.bytes = buffer;
        this.timestamp = timestamp;
    }

    @Override
    public Timestamp getTimeStamp() {
        return timestamp;
    }

    @Override
    public byte[] asBytes() {
        return bytes;
    }

    @Override
    public int size() {
        return bytes.length;
    }
}
