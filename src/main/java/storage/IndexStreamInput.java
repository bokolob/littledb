package storage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import row.Key;
import row.KeyImpl;
import row.Timestamp;
import row.TimestampImpl;

import static row.Key.KEY_SIZE;

public class IndexStreamInput implements AutoCloseable {
    DataInputStream inputStream;

    public IndexStreamInput(InputStream out) {
        this.inputStream = new DataInputStream(new BufferedInputStream(out));
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public PrimaryIndex.IndexEntry readRecord() throws IOException {

        Key key = new KeyImpl(inputStream.readNBytes(KEY_SIZE));
        int offset = inputStream.readInt();
        long length = inputStream.readLong();
        Timestamp timestamp = new TimestampImpl(inputStream.readLong());

        return new PrimaryIndex.IndexEntry(key, offset, (int) length, timestamp);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
