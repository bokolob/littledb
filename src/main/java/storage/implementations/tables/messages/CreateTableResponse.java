package storage.implementations.tables.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;

public class CreateTableResponse extends BaseActorResponseImpl<CreateTableRequest.CreateParams, Boolean> {
    public CreateTableResponse(ActorRequest<CreateTableRequest.CreateParams, Boolean> sourceRequest, Boolean response) {
        super(sourceRequest, response);
    }
}
