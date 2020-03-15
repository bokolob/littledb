package storage.implementations.tables.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import row.Value;
import storage.implementations.tables.fields.BooleanField;
import storage.implementations.tables.fields.DoubleField;
import storage.implementations.tables.fields.IntField;
import storage.implementations.tables.fields.LongField;
import storage.implementations.tables.fields.StringField;

public class ValueParser {

    public ValueParser() {
    }

    public ParsedValue parse(TableSchema tableSchema, Value value) throws IOException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(value.asBytes()));

        Map<String, Field> result = new HashMap<>();

        for (var column : tableSchema.getColumns()) {
            switch (column.getColumnType()) {
                case INT:
                    result.put(column.getName(), new IntField(inputStream.readInt()));
                    break;
                case LONG:
                    result.put(column.getName(), new LongField(inputStream.readLong()));
                    break;
                case DOUBLE:
                    result.put(column.getName(), new DoubleField(inputStream.readDouble()));
                    break;
                case BOOLEAN:
                    result.put(column.getName(), new BooleanField(inputStream.readBoolean()));
                    break;
                case STRING:
                    int len = inputStream.readInt();

                    if (len < 0) {
                        throw new IllegalArgumentException();
                    }

                    byte[] bytes = new byte[len];
                    int rc = inputStream.read(bytes);

                    if (rc < len) {
                        throw new IllegalArgumentException();
                    }
                    result.put(column.getName(), new StringField(new String(bytes)));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        return new ParsedValue(result);
    }

}
