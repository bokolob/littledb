package storage.implementations.tables.data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ParsedValue {
    private final Map<String, Field> fields;

    public ParsedValue(Map<String, Field> fields) {
        this.fields = fields;
    }

    @JsonProperty("fields")
    public Map<String, Field> getFields() {
        return fields;
    }

    public Field getFieldByColumnName(String name) {
        if (!fields.containsKey(name)) {
            throw new IllegalArgumentException("Unknown field " + name);
        }

        return fields.get(name);
    }

    @Override
    public String toString() {
        return "ParsedValue{" +
                "fields=" + fields +
                '}';
    }
}
