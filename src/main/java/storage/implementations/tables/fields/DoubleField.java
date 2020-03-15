package storage.implementations.tables.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import storage.implementations.tables.data.Field;

public class DoubleField implements Field {
    private final double value;

    public DoubleField(double value) {
        this.value = value;
    }

    public static Field fromString(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("");
        }

        return new DoubleField(Double.parseDouble(rawValue));
    }

    @Override
    public String asString() {
        return Double.toString(value);
    }

    @Override
    public String toString() {
        return "DoubleField{" +
                "value=" + value +
                '}';
    }

    @JsonProperty("value")
    public double getDouble() {
        return value;
    }
}
