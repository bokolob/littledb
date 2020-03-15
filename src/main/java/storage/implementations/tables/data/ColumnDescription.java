package storage.implementations.tables.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnDescription {
    private final ColumnTypes columnTypes;
    private final String name;

    @JsonCreator
    public ColumnDescription(@JsonProperty("type") ColumnTypes columnTypes, @JsonProperty("name") String name) {
        this.columnTypes = columnTypes;
        this.name = name;
    }

    @JsonProperty("type")
    public ColumnTypes getColumnType() {
        return columnTypes;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
}
