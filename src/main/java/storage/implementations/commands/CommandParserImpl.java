package storage.implementations.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import row.Key;
import row.KeyImpl;
import storage.ReplicationService;
import storage.implementations.commands.messages.ClientRequest;
import storage.implementations.commands.messages.ClientResponse;
import storage.implementations.tables.data.ColumnDescription;
import storage.implementations.tables.data.ColumnTypes;
import storage.implementations.tables.data.TableInfo;
import storage.implementations.tables.data.TableSchema;
import storage.implementations.tables.messages.CreateTableRequest;
import storage.implementations.tables.messages.TableListRequest;
import storage.implementations.tables.messages.TableLookupRequest;
import storage.implementations.tables.messages.TableSetRequest;

public class CommandParserImpl extends BaseActorImpl {
    private final ReplicationService replicationService;
    private final String nodeName;
    private final ActorMessageRouter actorMessageRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommandParserImpl(int threadCount,
                             ReplicationService replicationService,
                             String nodeName, ActorMessageRouter actorMessageRouter) {
        super(threadCount, actorMessageRouter);
        this.replicationService = replicationService;
        this.nodeName = nodeName;
        this.actorMessageRouter = actorMessageRouter;

        this.registerMessageHandler(ClientRequest.class, this::processCommand);
    }

    private void processCommand(ClientRequest request) {
        String[] kv = request.getRequestObject().split(" ");

        if (kv.length < 2) {
            actorMessageRouter.sendResponse(new ClientResponse(request, "Bad command\n"));
            return;
        }

        switch (kv[0]) {
            case "list_tables":
                TableListRequest tableListRequest = new TableListRequest(this, null,
                        v -> actorMessageRouter.sendResponse(new ClientResponse(request,
                                v.map(this::prepareTablesList).orElseGet("No tables found\n"::toString)))
                );

                actorMessageRouter.sendRequest(tableListRequest);

                break;
            case "get":
                String tableName = kv[1];
                Key key = new KeyImpl(kv[2].getBytes());

                TableLookupRequest tableLookupRequest = new TableLookupRequest(this,
                        new TableLookupRequest.TableLookupParams(tableName, key),
                        v -> actorMessageRouter.sendResponse(new ClientResponse(request,
                                v.map(e -> {
                                    try {
                                        return objectMapper.writeValueAsString(e);
                                    } catch (JsonProcessingException ex) {
                                        ex.printStackTrace();
                                        return "";
                                    }
                                }).orElseGet("Not found\n"::toString)))
                );

                actorMessageRouter.sendRequest(tableLookupRequest);
                break;
            case "set":
                String tName = kv[1];
                Key setKey = new KeyImpl(kv[2]);
                String[] pairs2 = kv[3].split(",");
                Map<String, String> fieldValues = new HashMap<>();

                for (String kv2 : pairs2) {
                    String[] pp = kv2.split("=");
                    fieldValues.put(pp[0], pp[1]);
                }

                TableSetRequest tableSetRequest = new TableSetRequest(this,
                        new TableSetRequest.TableSetParams(tName, setKey, fieldValues),
                        v -> actorMessageRouter.sendResponse(new ClientResponse(request, "Ok\n")));

                //ReplicationRequest ...
                actorMessageRouter.sendRequest(tableSetRequest);
                break;
            case "create_table":
                String name = kv[1];
                String definition = kv[2];
                String[] pairs = definition.split(",");

                List<ColumnDescription> columnDescriptions = new ArrayList<>();

                for (String pair : pairs) {
                    String[] pp = pair.split("=");
                    ColumnTypes types = ColumnTypes.valueOf(pp[1].toUpperCase());
                    columnDescriptions.add(new ColumnDescription(types, pp[0]));
                }

                TableSchema tableSchema = new TableSchema(columnDescriptions);

                actorMessageRouter.sendRequest(new CreateTableRequest(this,
                        new CreateTableRequest.CreateParams(name, tableSchema),
                        v -> actorMessageRouter.sendResponse(new ClientResponse(request, v.orElse(false) ? "Ok" :
                                "failed"))
                ));

                break;
            case "ping":
                actorMessageRouter.sendResponse(new ClientResponse(request, "pong"));
                break;
            case "getName":
                actorMessageRouter.sendResponse(new ClientResponse(request, nodeName + "\n"));
                break;
            default:
                actorMessageRouter.sendResponse(new ClientResponse(request, "Unknown command\n"));
                break;
        }

    }

    private String prepareTablesList(List<TableInfo> tableInfos) {
        StringBuilder stringBuilder = new StringBuilder();

        for (TableInfo tableInfo : tableInfos) {
            stringBuilder.append(tableInfo.getTableName());
            stringBuilder.append(": ");
            for (ColumnDescription col : tableInfo.getTableSchema().getColumns()) {
                stringBuilder.append(col.getName());
                stringBuilder.append("=");
                stringBuilder.append(col.getColumnType());
                stringBuilder.append('\t');
            }

        }

        return stringBuilder.toString();
    }

}
