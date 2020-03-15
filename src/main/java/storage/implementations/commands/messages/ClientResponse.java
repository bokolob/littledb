package storage.implementations.commands.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;

public class ClientResponse extends BaseActorResponseImpl<String, String> {

    public ClientResponse(ActorRequest<String, String> sourceRequest, String response) {
        super(sourceRequest, response);
    }
}
