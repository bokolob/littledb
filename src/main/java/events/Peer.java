package events;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Peer {
    BlockingQueue<ByteBuffer[]> getMessagesForSend();

    MessageReader getMessageReader();

    Function<SocketChannel, Peer> getOnAccept();

    Consumer<Peer> getOnConnect();

    Consumer<Peer> getOnFailure();

    Consumer<Message> getOnNewMessage();

    void setOnAccept(Function<SocketChannel, Peer> onAccept);

    void setOnConnect(Consumer<Peer> onConnect);

    void setOnNewMessage(Consumer<Message> onConnect);

    void setOnWritable(Consumer<Peer> onWritable);

    Consumer<Peer> getOnWritable();

    void setOnFailure(Consumer<Peer> onFailure);

    ByteBuffer getReadBuffer();

    SelectionKey getSelectionKey();

    SelectionKey register(SelectableChannel channel, Selector selector, int ops) throws ClosedChannelException;

    void close();
}
