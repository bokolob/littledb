package storage.implementations.tables.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import storage.implementations.tables.data.Field;

public class BooleanField implements Field {
    private final boolean value;

    public BooleanField(boolean value) {
        this.value = value;
    }

    public static Field fromString(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("");
        }

        return new BooleanField(Boolean.parseBoolean(rawValue));
    }

    @Override
    public String asString() {
        return Boolean.toString(value);
    }

    @JsonProperty("value")
    public boolean getBoolean() {
        return value;
    }

    @Override
    public String toString() {
        return "BooleanField{" +
                "value=" + value +
                '}';
    }
}
