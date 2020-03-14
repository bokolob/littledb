package storage.implementations.disk.messages.lookup;

import java.util.Optional;

import actors.ActorRequest;
import actors.ActorResponse;
import row.Key;
import row.Value;

public class LookupResponse extends LookupMessage implements ActorResponse<Key, Value> {
    private final Value value;
    private final LookupRequest request;

    public LookupResponse(Value value, LookupRequest request) {
        this.value = value;
        this.request = request;
    }

    @Override
    public ActorRequest<Key, Value> getSourceRequest() {
        return request;
    }

    @Override
    public Optional<Value> getResponseObject() {
        return Optional.ofNullable(value);
    }
}
