package events.implementations;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import events.Message;
import events.MessageReader;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageReaderImplTest {

    @Test
    public void consumeOkTest() {
        String t1 = "A 123456 hello world\nR  3453 second message\n";
        ByteBuffer b1 = ByteBuffer.wrap(t1.getBytes());
        MessageReader reader = new MessageReaderImpl();

        reader.consume(b1);

        List<Message> messageList = new ArrayList<>();

        while (reader.hasNext()) {
            messageList.add(reader.next());
        }

        assertEquals("Got two messages", 2, messageList.size());

        assertEquals(Message.MessageType.RESPONSE, messageList.get(0).getType());
        assertEquals(123456L, messageList.get(0).getMessageId());
        assertEquals("hello world", new String(messageList.get(0).payload()));

        assertEquals(Message.MessageType.COMMAND, messageList.get(1).getType());
        assertEquals(3453L, messageList.get(1).getMessageId());
        assertEquals("second message", new String(messageList.get(1).payload()));
    }

    @Test
    public void consumeUnparsableTest() {
        String t1 = "An garbage message\nR  3453 second message\n";
        ByteBuffer b1 = ByteBuffer.wrap(t1.getBytes());
        MessageReader reader = new MessageReaderImpl();

        reader.consume(b1);

        List<Message> messageList = new ArrayList<>();

        while (reader.hasNext()) {
            messageList.add(reader.next());
        }

        assertEquals("Got two messages", 2, messageList.size());

        assertEquals(Message.MessageType.UNPARSABLE, messageList.get(0).getType());
        assertEquals(0L, messageList.get(0).getMessageId());
        assertEquals("", new String(messageList.get(0).payload()));

        assertEquals(Message.MessageType.COMMAND, messageList.get(1).getType());
        assertEquals(3453L, messageList.get(1).getMessageId());
        assertEquals("second message", new String(messageList.get(1).payload()));
    }

}