package storage.implementations.commands;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import row.Key;
import row.KeyImpl;
import row.Row;
import row.RowImpl;
import row.TimestampImpl;
import row.Value;
import row.ValueImpl;
import storage.ReplicationService;
import storage.implementations.commands.messages.ClientRequest;
import storage.implementations.commands.messages.ClientRespons;
import storage.implementations.disk.messages.lookup.LookupRequest;
import storage.implementations.disk.messages.set.SetKVRequest;

public class CommandParserImpl extends BaseActorImpl {
    private final ReplicationService replicationService;
    private final String nodeName;
    private final ActorMessageRouter actorMessageRouter;

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
            System.err.println("Bad command..");
            actorMessageRouter.sendResponse(new ClientRespons(request, "Bad command\n"));
            return;
        }

        switch (kv[0]) {
            case "get":
                System.err.println("KEY=" + kv[1] + " " + new String(new KeyImpl(kv[1].getBytes()).toBytes()));

                Key key = new KeyImpl(kv[1].getBytes());

                LookupRequest lookupRequest = new LookupRequest(this, key,
                        v -> actorMessageRouter.sendResponse(new ClientRespons(request,
                                v.map(Value::toString).orElseGet("Not found\n"::toString)))
                );

                actorMessageRouter.sendRequest(lookupRequest);
                break;
            case "set":

                Key setKey = new KeyImpl(kv[1].getBytes());
                Value setValue = new ValueImpl(kv[2].getBytes(), new TimestampImpl());
                Row row = new RowImpl(setKey, setValue);

                SetKVRequest setKVRequest = new SetKVRequest(this, row, v ->
                        actorMessageRouter.sendResponse(new ClientRespons(request, "Ok\n")));
                //ReplicationRequest ...
                actorMessageRouter.sendRequest(setKVRequest);
                break;
            case "ping":
                actorMessageRouter.sendResponse(new ClientRespons(request, "pong\n"));
                break;
            case "getName":
                actorMessageRouter.sendResponse(new ClientRespons(request, nodeName + "\n"));
                break;
            default:
                actorMessageRouter.sendResponse(new ClientRespons(request, "Unknown command\n"));
                break;
        }

    }

}
