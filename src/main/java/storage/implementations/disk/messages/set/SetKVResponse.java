package storage.implementations.disk.messages.set;

import java.util.Optional;

import actors.ActorRequest;
import actors.ActorResponse;
import row.Row;

public class SetKVResponse extends SetKVMessage implements ActorResponse<Row, Void> {
    private final ActorRequest<Row, Void> actorRequest;

    public SetKVResponse(ActorRequest<Row, Void> actorRequest) {
        this.actorRequest = actorRequest;
    }

    @Override
    public ActorRequest<Row, Void> getSourceRequest() {
        return actorRequest;
    }

    @Override
    public Optional<Void> getResponseObject() {
        return Optional.empty();
    }
}
