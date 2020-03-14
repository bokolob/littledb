package storage.implementations.commands.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.ActorRequest;

public class ClientRequest extends ClientMessage implements ActorRequest<String, String> {
    private final Actor sourceActorId;
    private final Consumer<Optional<String>> consumer;
    private final String response;

    public ClientRequest(Actor sourceActorId, String response, Consumer<Optional<String>> consumer) {
        this.sourceActorId = sourceActorId;
        this.consumer = consumer;
        this.response = response;
    }

    @Override
    public Actor getSourceActor() {
        return sourceActorId;
    }

    @Override
    public Consumer<Optional<String>> getResponseHandler() {
        return consumer;
    }

    @Override
    public String getRequestObject() {
        return response;
    }
}
