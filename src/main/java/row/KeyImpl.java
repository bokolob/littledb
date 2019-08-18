package row;

import java.util.Arrays;

public class KeyImpl implements Key {

    byte[] content;

    @Override
    public String toString() {
        return "KeyImpl{" +
                "content=" + new String(content) +
                '}';
    }

    public KeyImpl(String str) {
        this(str.getBytes());
    }

    public KeyImpl(byte[] bytes) {
        content = Arrays.copyOfRange(bytes, 0, Key.KEY_SIZE);
    }

    @Override
    public byte[] toBytes() {
        return content;
    }

    @Override
    public int compareTo(Key o) {

        for (int i = 0; i < content.length; i++) {

            if (content[i] < o.toBytes()[i]) {
                return -1;
            } else if (content[i] > o.toBytes()[i]) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyImpl key = (KeyImpl) o;
        return Arrays.equals(content, key.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
}
