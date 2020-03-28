package storage.implementations.disk.persistence.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import storage.implementations.disk.VolatileGeneration;

public class AddGenerationResponse extends BaseActorResponseImpl<VolatileGeneration, Void> {
    public AddGenerationResponse(ActorRequest<VolatileGeneration, Void> sourceRequest, Void response) {
        super(sourceRequest, response);
    }
}
