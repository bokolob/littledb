package events.implementations;

import java.nio.ByteBuffer;

import events.Message;
import events.MessageWriter;

public class MessageWriterImpl implements MessageWriter {
    public ByteBuffer[] asByteBufferSequence(Message message) {
        ByteBuffer[] byteBuffers = new ByteBuffer[3];
        byteBuffers[0] = ByteBuffer.allocate(14); //1 byte for type, one for space and 12 for maximum long value
        byteBuffers[1] = ByteBuffer.wrap(message.payload());
        byteBuffers[2] = ByteBuffer.wrap("\n".getBytes());
        char type;

        switch (message.getType()) {
            case RESPONSE:
                type = 'A';
                break;
            case COMMAND:
                type = 'R';
                break;
            case UNPARSABLE:
            default:
                throw new IllegalArgumentException();
        }


        byteBuffers[0]
                .asCharBuffer()
                .put(type)
                .put(' ')
                .put(Long.toString(message.getMessageId()))
                .put(' ')
        ;

        //byteBuffers[0].flip();

        return byteBuffers;
    }
}
