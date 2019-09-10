package storage.implementations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import row.Key;
import row.KeyImpl;
import row.TimestampImpl;
import row.Value;
import row.ValueImpl;
import storage.CommandParser;
import storage.ReplicationService;
import storage.StorageService;

public class CommandParserImpl implements CommandParser {
    private StorageService storageService;
    private ReplicationService replicationService;
    private String nodeName;

    public CommandParserImpl(StorageService storageService, ReplicationService replicationService,
            String nodeName)
    {
        this.storageService = storageService;
        this.replicationService = replicationService;
        this.nodeName = nodeName;
    }

    @Override
    public CompletableFuture<byte[]> processCommand(String command) throws IOException {
        String[] kv = command.split(" ");

        if (kv.length < 2) {
            System.err.println("Bad command..");

            return CompletableFuture.completedFuture("Bad command\n".getBytes());
        }

        switch (kv[0]) {
            case "get":
                System.err.println("KEY=" + kv[1] + " " + new String(new KeyImpl(kv[1].getBytes()).toBytes()));

                Key key = new KeyImpl(kv[1].getBytes());

                return storageService
                        .get(key)
                        .thenApply(v ->
                                v.map(Value::asBytes)
                                        .orElseGet("Not found\n"::getBytes));

            case "set":
                Key setKey = new KeyImpl(kv[1].getBytes());
                Value setValue = new ValueImpl(kv[2].getBytes(), new TimestampImpl());
                return storageService
                        .set(setKey, setValue)
                        .thenApply(v -> {
                            replicationService.replicate(setKey, setValue);
                            return v;
                        })
                        .thenApply(v -> "Ok\n".getBytes());
            case "ping":
                return CompletableFuture.completedFuture("pong\n".getBytes());
            case "getName":
                return CompletableFuture.completedFuture(nodeName.getBytes());
            default:
                return CompletableFuture.completedFuture("Unknown command\n".getBytes());
        }

    }

    private ByteBuffer sb(String msg) {
        return ByteBuffer.wrap(msg.getBytes());
    }

}
