package storage.implementations.disk.messages.set;

import actors.ActorMessage;
import row.Row;

public abstract class SetKVMessage implements ActorMessage<Row, Void> {
    @Override
    public Class<Void> getResponseDataType() {
        return Void.class;
    }

    @Override
    public Class<Row> getRequestDataType() {
        return Row.class;
    }
}
