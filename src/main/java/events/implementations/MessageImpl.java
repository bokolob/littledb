package events.implementations;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import events.Message;

public class MessageImpl implements Message {
    private final long messageId;
    private final byte[] data;
    private final Message.MessageType type;
    private ByteBuffer[] bufferForWriting;

    public static class MessageImplBuilder {
        private long messageId;
        private final ByteArrayOutputStream payloadWriter;
        private MessageType type;

        public MessageImplBuilder() {
            this.payloadWriter = new ByteArrayOutputStream();
        }

        public MessageImplBuilder setType(MessageType type) {
            this.type = type;
            return this;
        }

        public MessageImplBuilder setMessageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public MessageImplBuilder addPayload(byte b) {
            payloadWriter.write(b);
            return this;
        }

        public MessageImpl build() {
            return new MessageImpl(messageId, payloadWriter.toByteArray(), type);
        }

    }

    public MessageImpl(long messageId, byte[] payload, MessageType type) {
        this.messageId = messageId;
        this.type = type;
        this.data = payload;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public byte[] payload() {
        return data;
    }


}
