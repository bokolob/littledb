package storage.implementations.disk.messages.set;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import row.Row;

public class SetKVRequest extends BaseActorRequestImpl<Row, Void> {
    public SetKVRequest(Actor sourceActor,  Row requestObject, Consumer<Optional<Void>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
