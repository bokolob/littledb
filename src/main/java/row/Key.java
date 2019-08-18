package row;

public interface Key extends Comparable<Key> {
    int KEY_SIZE = 16;
    byte[] toBytes();
}
