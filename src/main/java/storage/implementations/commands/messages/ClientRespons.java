package storage.implementations.commands.messages;

import java.util.Optional;

import actors.ActorRequest;
import actors.ActorResponse;

public class ClientRespons extends ClientMessage implements ActorResponse<String, String> {
    private final ActorRequest<String, String> sourceRequest;
    private final String response;

    public ClientRespons(ActorRequest<String, String> sourceRequest, String response) {
        this.sourceRequest = sourceRequest;
        this.response = response;
    }

    @Override
    public ActorRequest<String, String> getSourceRequest() {
        return sourceRequest;
    }

    @Override
    public Optional<String> getResponseObject() {
        return Optional.ofNullable(response);
    }
}
