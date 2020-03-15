package storage.implementations.tables.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import storage.implementations.tables.data.Field;

public class IntField implements Field {
    private final int value;

    public IntField(int value) {
        this.value = value;
    }

    public static Field fromString(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("");
        }

        return new IntField(Integer.parseInt(rawValue));
    }

    @Override
    public String asString() {
        return Integer.toString(value);
    }

    @JsonProperty("value")
    public int getInt() {
        return value;
    }

    @Override
    public String toString() {
        return "IntField{" +
                "value=" + value +
                '}';
    }
}
