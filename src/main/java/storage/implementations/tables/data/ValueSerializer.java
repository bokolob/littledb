package storage.implementations.tables.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import row.Timestamp;
import row.Value;
import row.ValueImpl;
import storage.implementations.tables.fields.BooleanField;
import storage.implementations.tables.fields.DoubleField;
import storage.implementations.tables.fields.IntField;
import storage.implementations.tables.fields.LongField;

public class ValueSerializer {

    public ValueSerializer() {
    }

    public Value serialize(TableSchema tableSchema, ParsedValue parsedValue, Timestamp timestamp) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        for (var column : tableSchema.getColumns()) {

            Field field = parsedValue.getFieldByColumnName(column.getName());

            switch (column.getColumnType()) {
                case INT:
                    IntField f = (IntField) field;
                    outputStream.writeInt(f.getInt());
                    break;
                case LONG:
                    LongField lf = (LongField) field;
                    outputStream.writeLong(lf.getLong());
                    break;
                case DOUBLE:
                    DoubleField df = (DoubleField) field;
                    outputStream.writeDouble(df.getDouble());
                    break;
                case BOOLEAN:
                    BooleanField bf = (BooleanField) field;
                    outputStream.writeBoolean(bf.getBoolean());
                    break;
                case STRING:
                    outputStream.writeInt(field.asString().length());
                    outputStream.write(field.asString().getBytes());
                    break;
                default:
                    throw new IllegalArgumentException();
            }

        }

        outputStream.flush();
        outputStream.close();

        return new ValueImpl(byteArrayOutputStream.toByteArray(), timestamp);
    }
}
