package storage.implementations.disk.messages.lookup;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.ActorRequest;
import row.Key;
import row.Value;

public class LookupRequest extends LookupMessage implements ActorRequest<Key, Value> {
    private final Actor sourceId;
    private final Key key;
    private final Consumer<Optional<Value>> processResponse;

    public LookupRequest(Actor sourceId, Key key, Consumer<Optional<Value>> processResponse) {
        this.sourceId = sourceId;
        this.key = key;
        this.processResponse = processResponse;
    }

    @Override
    public Actor getSourceActor() {
        return sourceId;
    }

    @Override
    public Consumer<Optional<Value>> getResponseHandler() {
        return processResponse;
    }

    @Override
    public Key getRequestObject() {
        return key;
    }
}
