package storage.implementations.disk.persistence.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import row.Key;
import row.Value;

public class FileLookupRequest extends BaseActorRequestImpl<Key, Value> {
    public FileLookupRequest(Actor sourceActor, Key requestObject, Consumer<Optional<Value>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
