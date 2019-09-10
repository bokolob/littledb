package events.implementations;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import events.Message;
import events.MessageReader;

import static events.implementations.MessageReaderImpl.State.ERROR;
import static events.implementations.MessageReaderImpl.State.NONE;
import static events.implementations.MessageReaderImpl.State.NUMBER;
import static events.implementations.MessageReaderImpl.State.PAYLOAD;
import static events.implementations.MessageReaderImpl.State.SPACE1;
import static events.implementations.MessageReaderImpl.State.SPACE2;
import static events.implementations.MessageReaderImpl.State.TYPE;

public class MessageReaderImpl implements MessageReader {
    private Deque<Message> messages = new ArrayDeque<>();

    enum State {
        NONE,
        TYPE,
        SPACE1,
        NUMBER,
        SPACE2,
        PAYLOAD,
        ERROR
    }

    private State state = NONE;
    private MessageImpl.MessageImplBuilder builder;
    private long tmp = 0;

    @Override
    public void consume(ByteBuffer incoming) {

        incoming.flip();

        for (int i = incoming.position(); i < incoming.limit(); ) {
            byte b = incoming.get(i);
            switch (state) {
                case NONE:
                    builder = new MessageImpl.MessageImplBuilder();
                    tmp = 0;
                    state = TYPE;
                    break;
                case TYPE:
                    if (b == 'A') {
                        builder.setType(Message.MessageType.RESPONSE);
                    } else if (b == 'R') {
                        builder.setType(Message.MessageType.COMMAND);
                    } else if (b == ' ') {
                        state = SPACE1;
                        break;
                    } else {
                        state = ERROR;
                        break;
                    }

                    i++;
                    break;
                case SPACE1:
                    if (b == ' ') {
                        i++;
                    } else {
                        state = NUMBER;
                    }
                    break;
                case NUMBER:
                    if (b == ' ') {
                        if (tmp == 0) {
                            state = ERROR;
                            break;
                        }

                        builder.setMessageId(tmp);
                        tmp = 0;
                        state = SPACE2;
                        break;
                    }

                    int d = b - '0';

                    if (d < 0 || d > 9) {
                        state=ERROR;
                        break;
                    }

                    tmp *= 10;
                    tmp += d;
                    i++;
                    break;
                case SPACE2:
                    if (b == ' ') {
                        i++;
                    } else {
                        state = PAYLOAD;
                    }
                    break;
                case PAYLOAD:
                    if (b == '\n') {
                        messages.addLast(builder.build());
                        state = NONE;
                    } else {
                        builder.addPayload(b);
                    }
                    i++;
                    break;
                case ERROR:
                    if (b == '\n') {
                        builder.setType(Message.MessageType.UNPARSABLE);
                        messages.addLast(builder.build());
                        state = NONE;
                    }
                    i++;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }


    }

    @Override
    public boolean hasNext() {
        return !messages.isEmpty();
    }

    @Override
    public Message next() {
        return messages.pollFirst();
    }
}
