package storage.implementations.tables.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import storage.implementations.tables.data.Field;

public class StringField implements Field {
    private final String value;

    public StringField(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "StringField{" +
                "value='" + value + '\'' +
                '}';
    }

    public static Field fromString(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("");
        }

        return new StringField(rawValue);
    }

    @Override
    @JsonProperty("value")
    public String asString() {
        return value;
    }

}
