package storage.implementations.commands.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;

public class ClientRequest extends BaseActorRequestImpl<String, String> {
    public ClientRequest(Actor sourceActor, String requestObject, Consumer<Optional<String>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
