package storage.implementations.disk.messages.lookup;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import row.Key;
import row.Value;

public class LookupRequest extends BaseActorRequestImpl<Key, Value> {
    public LookupRequest(Actor sourceActor, Key requestObject, Consumer<Optional<Value>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
