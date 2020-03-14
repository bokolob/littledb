package storage.implementations.disk.messages.lookup;

import actors.ActorMessage;
import row.Key;
import row.Value;

public abstract class LookupMessage implements ActorMessage<Key, Value> {
    @Override
    public Class<Value> getResponseDataType() {
        return Value.class;
    }

    @Override
    public Class<Key> getRequestDataType() {
        return Key.class;
    }
}
