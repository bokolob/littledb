package storage.implementations.commands.messages;

import actors.ActorMessage;

public abstract class ClientMessage implements ActorMessage<String, String> {

    @Override
    public Class<String> getResponseDataType() {
        return String.class;
    }

    @Override
    public Class<String> getRequestDataType() {
        return String.class;
    }
}
