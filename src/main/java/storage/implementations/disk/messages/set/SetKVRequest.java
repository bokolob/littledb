package storage.implementations.disk.messages.set;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.ActorRequest;
import row.Row;

public class SetKVRequest extends SetKVMessage implements ActorRequest<Row, Void> {
    private final Actor sourceActorId;
    private final Row row;
    private final Consumer<Optional<Void>> consumer;

    public SetKVRequest(Actor sourceActorId, Row row, Consumer<Optional<Void>> consumer) {
        this.sourceActorId = sourceActorId;
        this.row = row;
        this.consumer = consumer;
    }

    @Override
    public Actor getSourceActor() {
        return sourceActorId;
    }

    @Override
    public Consumer<Optional<Void>> getResponseHandler() {
        return consumer;
    }

    @Override
    public Row getRequestObject() {
        return row;
    }
}
