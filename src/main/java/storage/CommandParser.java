package storage;

import java.io.IOException;

public interface CommandParser {
    byte[] processCommand(String command, AsyncWriteCallback callback) throws IOException;
}
