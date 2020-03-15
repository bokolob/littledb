package storage.implementations.tables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import row.Row;
import row.RowImpl;
import row.TimestampImpl;
import row.Value;
import storage.implementations.VolatileGenerationImpl;
import storage.implementations.disk.DiskTablesService;
import storage.implementations.disk.DiskTablesServiceImpl;
import storage.implementations.disk.messages.lookup.LookupRequest;
import storage.implementations.disk.messages.set.SetKVRequest;
import storage.implementations.tables.data.Field;
import storage.implementations.tables.data.ParsedValue;
import storage.implementations.tables.data.TableInfo;
import storage.implementations.tables.data.TableSchema;
import storage.implementations.tables.data.ValueParser;
import storage.implementations.tables.data.ValueSerializer;
import storage.implementations.tables.fields.BooleanField;
import storage.implementations.tables.fields.DoubleField;
import storage.implementations.tables.fields.IntField;
import storage.implementations.tables.fields.LongField;
import storage.implementations.tables.fields.StringField;
import storage.implementations.tables.messages.CreateTableRequest;
import storage.implementations.tables.messages.CreateTableResponse;
import storage.implementations.tables.messages.TableListRequest;
import storage.implementations.tables.messages.TableListResponse;
import storage.implementations.tables.messages.TableLookupRequest;
import storage.implementations.tables.messages.TableLookupResponse;
import storage.implementations.tables.messages.TableSetRequest;
import storage.implementations.tables.messages.TableSetResponse;

public class TablesListServiceImpl extends BaseActorImpl {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final File root;
    private final ValueSerializer valueSerializer;
    private final ValueParser valueParser;
    private final Map<String, TableInfo> tables;
    private final ActorMessageRouter router;

    public TablesListServiceImpl(int threadCount, ActorMessageRouter router, String path,
                                 ValueSerializer valueSerializer, ValueParser valueParser) {
        super(threadCount, router);
        root = new File(path);

        if (!root.exists()) {
            root.mkdir();
        }

        this.valueSerializer = valueSerializer;
        this.valueParser = valueParser;
        tables = new ConcurrentHashMap<>();
        this.router = router;
        initTables();

        this.registerMessageHandler(TableListRequest.class, this::handleListRequest);
        this.registerMessageHandler(CreateTableRequest.class, this::createTableHandler);
        this.registerMessageHandler(TableLookupRequest.class, this::lookupHandler);
        this.registerMessageHandler(TableSetRequest.class, this::setHandler);
    }

    private void setHandler(TableSetRequest tableSetRequest) {
        TableInfo tableInfo = tables.get(tableSetRequest.getRequestObject().getTable());

        if (tableInfo == null) {
            System.err.println("Unknown table " + tableSetRequest.getRequestObject().getTable());
            router.sendResponse(new TableSetResponse(tableSetRequest, null));
        }

        try {
            Row row = new RowImpl(
                    tableSetRequest.getRequestObject().getKey(),
                    tableInfo.getValueSerializer().serialize(tableInfo.getTableSchema(),
                            prepareForSet(tableInfo.getTableSchema(), tableSetRequest.getRequestObject().getFields()),
                            new TimestampImpl())
            );

            tableInfo.getDiskTablesService().pushMessage(
                    new SetKVRequest(this, row,
                            v -> router.sendResponse(new TableSetResponse(tableSetRequest, true)))
            );

        } catch (Exception e) {
            e.printStackTrace();
            router.sendResponse(new TableSetResponse(tableSetRequest, null));
        }

    }

    ParsedValue prepareForSet(TableSchema schema, Map<String, String> userValues) {
        Map<String, Field> fields = new HashMap<>();
        //TODO validation
        for (var col : schema.getColumns()) {
            String rawValue = userValues.get(col.getName());
            switch (col.getColumnType()) {
                case INT:
                    fields.put(col.getName(), IntField.fromString(rawValue));
                    break;
                case LONG:
                    fields.put(col.getName(), LongField.fromString(rawValue));
                    break;
                case DOUBLE:
                    fields.put(col.getName(), DoubleField.fromString(rawValue));
                    break;
                case STRING:
                    fields.put(col.getName(), StringField.fromString(rawValue));
                    break;
                case BOOLEAN:
                    fields.put(col.getName(), BooleanField.fromString(rawValue));
                    break;
            }
        }

        return new ParsedValue(fields);
    }

    void lookupHandler(TableLookupRequest tableLookupRequest) {
        TableInfo tableInfo = tables.get(tableLookupRequest.getRequestObject().getTable());

        if (tableInfo == null) {
            System.err.println("Unknown table " + tableLookupRequest.getRequestObject().getTable());
            router.sendResponse(new TableLookupResponse(tableLookupRequest, null));
        }

        //todo do it through router
        tableInfo.getDiskTablesService().pushMessage(
                new LookupRequest(this, tableLookupRequest.getRequestObject().getKey(),
                        value -> processDiskLookupResponse(tableLookupRequest, tableInfo, value)));
    }

    private void processDiskLookupResponse(TableLookupRequest tableLookupRequest, TableInfo tableInfo,
                                           Optional<Value> value) {
        if (value.isEmpty()) {
            router.sendResponse(new TableLookupResponse(tableLookupRequest, null));
            return;
        }

        try {
            ParsedValue parsedValue = tableInfo.getValueParser().parse(tableInfo.getTableSchema(), value.get());
            router.sendResponse(new TableLookupResponse(tableLookupRequest, parsedValue));
        } catch (IOException e) {
            e.printStackTrace();
            router.sendResponse(new TableLookupResponse(tableLookupRequest, null));
        }
    }

    private void createTableHandler(CreateTableRequest t) {
        try {
            boolean rc = createTable(t.getRequestObject().getName(), t.getRequestObject().getSchema());
            router.sendResponse(new CreateTableResponse(t, rc));
        } catch (IOException e) {
            e.printStackTrace();
            router.sendResponse(new CreateTableResponse(t, false));
        }
    }

    private void handleListRequest(TableListRequest tableListRequest) {
        router.sendResponse(new TableListResponse(tableListRequest, List.copyOf(tables.values())));
    }

    void initTables() {

        for (File f : Objects.requireNonNull(root.listFiles())) {
            if (f.isDirectory()) {
                TableSchema schema = tryLoadSchema(new File(f, "/schema"));

                if (schema != null) {
                    tables.put(f.getName(), startDiskService(f.getName(), new File(f, "data"), schema));
                }

            }
        }
    }

    TableInfo startDiskService(String tableName, File dataPath, TableSchema tableSchema) {
        DiskTablesService diskTablesService = new DiskTablesServiceImpl(10, dataPath.getAbsolutePath(),
                VolatileGenerationImpl::new, router);

        diskTablesService.run();

        return new TableInfo(tableName, tableSchema, valueSerializer,
                valueParser, dataPath.getAbsolutePath(), diskTablesService);
    }

    private TableSchema tryLoadSchema(File s) {
        try {
            return mapper.readValue(s, TableSchema.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createTable(String name, TableSchema tableSchema) throws IOException {
        File f = new File(root, name);

        if (f.exists()) {
            return false;
        } else {
            f.mkdir();
        }

        File schema = new File(f, "schema");
        schema.createNewFile();

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(schema, tableSchema);

        tables.put(f.getName(), startDiskService(f.getName(), new File(f, "data"), tableSchema));

        return true;
    }
}
