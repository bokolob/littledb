package storage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import row.Value;

public class DataStreamOutput implements AutoCloseable {
    OutputStream outputStream;

    public DataStreamOutput(OutputStream out) {
        this.outputStream = new BufferedOutputStream(out);
    }

    public void writeRecord(Value value) throws IOException {
        outputStream.write(value.asBytes());
    }

    @Override
    public void close() throws Exception {
        outputStream.flush();
        outputStream.close();
    }
}
