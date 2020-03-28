package storage.implementations.disk.persistence.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import storage.implementations.disk.VolatileGeneration;

public class AddGenerationRequest extends BaseActorRequestImpl<VolatileGeneration, Void> {
    public AddGenerationRequest(Actor sourceActor,
                                VolatileGeneration requestObject, Consumer<Optional<Void>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
