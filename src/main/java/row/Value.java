package row;

public interface Value {
    Timestamp getTimeStamp();
    byte[] asBytes();
    int size();
}
