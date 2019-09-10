package storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface CommandParser {
    CompletableFuture<byte[]> processCommand(String command) throws IOException;
}
