package storage.implementations.tables.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import storage.implementations.tables.data.Field;

public class LongField implements Field {
    private final long value;

    public LongField(long value) {
        this.value = value;
    }

    public static Field fromString(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("");
        }

        return new LongField(Long.parseLong(rawValue));
    }

    @Override
    public String asString() {
        return Long.toString(value);
    }

    @JsonProperty("value")
    public long getLong() {
        return value;
    }

    @Override
    public String toString() {
        return "LongField{" +
                "value=" + value +
                '}';
    }
}
