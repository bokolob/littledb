package storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface CommandParser {
    CompletableFuture<ByteBuffer> processCommand(String command) throws IOException;
}
