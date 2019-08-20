package storage.implementations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import row.Key;
import row.KeyImpl;
import row.TimestampImpl;
import row.ValueImpl;
import storage.CommandParser;
import storage.StorageService;

public class CommandParserImpl implements CommandParser {
    StorageService storageService;
    ExecutorService executorService;

    public CommandParserImpl(StorageService storageService) {
        this.storageService = storageService;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<ByteBuffer> processCommand(String command) throws IOException {
        String[] kv = command.split(" ");

        if (kv.length < 2) {
            System.err.println("Bad command..");

            return CompletableFuture.completedFuture(ByteBuffer.wrap("Bad command\n".getBytes()));
        }


        if (kv[0].equals("get")) {

            System.err.println("KEY=" + kv[1] + " " + new String(new KeyImpl(kv[1].getBytes()).toBytes()));

            Key key = new KeyImpl(kv[1].getBytes());

            return storageService.get(key)
                    .thenApply(v ->
                            v.map(k -> ByteBuffer.wrap(k.asBytes()))
                                    .orElseGet(() -> ByteBuffer.wrap("Not found\n".getBytes()))
                    );

        } else if (kv[0].equals("set")) {
            return storageService.set(
                    new KeyImpl(kv[1].getBytes()),
                    new ValueImpl(kv[2].getBytes(), new TimestampImpl()))
                    .thenApply(v -> ByteBuffer.wrap("Ok\n".getBytes()));
        } else {
            return CompletableFuture.completedFuture(ByteBuffer.wrap("Unknown command\n".getBytes()));
        }
    }
}
