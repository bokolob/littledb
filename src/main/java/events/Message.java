package events;

public interface Message {
    enum MessageType {
        COMMAND,
        RESPONSE,
        UNPARSABLE
    }

    MessageType getType();

    long getMessageId();

    byte[] payload();

}
