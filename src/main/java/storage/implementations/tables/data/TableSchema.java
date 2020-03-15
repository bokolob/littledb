package storage.implementations.tables.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TableSchema {
    @JsonProperty("columns")
    private final List<ColumnDescription> columnDescriptionList;

    @JsonCreator
    public TableSchema(@JsonProperty("columns") List<ColumnDescription> columnDescriptionList) {
        this.columnDescriptionList = columnDescriptionList;
    }

    public List<? extends ColumnDescription> getColumns() {
        return columnDescriptionList;
    }
}
