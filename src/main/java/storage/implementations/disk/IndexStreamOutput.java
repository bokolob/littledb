package storage.implementations.disk;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import row.Key;
import row.Value;

public class IndexStreamOutput implements AutoCloseable {
    private DataOutputStream outputStream;
    private int offset = 0;

    public IndexStreamOutput(OutputStream out) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(out));
    }

    public PrimaryIndex.IndexEntry writeRecord(Key key, Value value) throws IOException {
        outputStream.write(key.toBytes());
        outputStream.writeInt(offset);
        outputStream.writeLong(value.size());
        outputStream.writeLong(value.getTimeStamp().asLong());

        PrimaryIndex.IndexEntry entry = new PrimaryIndex.IndexEntry(key, offset, value.size(), value.getTimeStamp());

        offset += value.size();
        return entry;
    }

    @Override
    public void close() throws Exception {
        outputStream.flush();
        outputStream.close();
    }
}
