package storage.implementations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;

public class AsyncLineReader {

    private final BiConsumer<SelectionKey, String> onNewLine;
    private final ByteBuffer readBuffer;
    private final StringBuffer stringBuffer;

    public AsyncLineReader(BiConsumer<SelectionKey, String> onNewLine) {
        this.onNewLine = onNewLine;
        stringBuffer = new StringBuffer();
        readBuffer = ByteBuffer.allocate(128);
        readBuffer.limit(0);
    }

    public int onIncomingData(SelectionKey key) throws IOException {
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
            int rc = ((SocketChannel)key.channel()).read(readBuffer);

            if (rc == -1) {
                return -1;
            }

            readBuffer.flip();
        }

        processNewData(key);
        return 0;
    }

    private void processNewData(SelectionKey key) {
        final int len = readBuffer.limit();

        for (int i = readBuffer.position(); i < len; i++) {
            byte b = readBuffer.get();

            if (b == '\n') {
                onNewLine.accept(key, stringBuffer.toString());
                stringBuffer.setLength(0);
            } else {
                stringBuffer.appendCodePoint(b);
            }
        }

    }

}
