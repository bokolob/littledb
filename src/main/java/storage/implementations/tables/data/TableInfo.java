package storage.implementations.tables.data;

import storage.implementations.disk.DiskTablesService;

public class TableInfo {
    private final String tableName;
    private final TableSchema tableSchema;
    private final ValueSerializer valueSerializer;
    private final ValueParser valueParser;
    private final String path;
    private final DiskTablesService diskTablesService;

    public TableInfo(String tableName, TableSchema tableSchema, ValueSerializer valueSerializer,
                     ValueParser valueParser, String path, DiskTablesService diskTablesService) {
        this.tableName = tableName;
        this.tableSchema = tableSchema;
        this.valueSerializer = valueSerializer;
        this.valueParser = valueParser;
        this.path = path;
        this.diskTablesService = diskTablesService;
    }

    public DiskTablesService getDiskTablesService() {
        return diskTablesService;
    }

    public String getPath() {
        return path;
    }

    public String getTableName() {
        return tableName;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public ValueSerializer getValueSerializer() {
        return valueSerializer;
    }

    public ValueParser getValueParser() {
        return valueParser;
    }

}
