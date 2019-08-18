package storage.implementations;

import java.io.IOException;

import row.KeyImpl;
import row.TimestampImpl;
import row.Value;
import row.ValueImpl;
import storage.CommandParser;
import storage.AsyncWriteCallback;
import storage.StorageService;

public class CommandParserImpl implements CommandParser {
    StorageService storageService;

    public CommandParserImpl(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public byte[] processCommand(String command, AsyncWriteCallback callback) throws IllegalArgumentException, IOException {
        String[] kv = command.split(" ");

        if (kv.length < 2) {
            System.err.println("Bad command..");
            return "Bad command".getBytes();
        }

        if (kv[0].equals("get")) {

            System.err.println("KEY=" + kv[1] + " " + new String(new KeyImpl(kv[1].getBytes()).toBytes()));

            Value value = storageService.get(new KeyImpl(kv[1].getBytes())).orElse(null);
            return value == null ? "not found".getBytes() : value.asBytes();
        } else if (kv[0].equals("set")) {
            storageService.set(new KeyImpl(kv[1].getBytes()),
                    new ValueImpl(kv[2].getBytes(), new TimestampImpl()));
            return "setted".getBytes();
        }

        return "Unknown command".getBytes();
    }
}
