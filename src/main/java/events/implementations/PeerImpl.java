package events.implementations;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import events.Message;
import events.MessageReader;
import events.Peer;

public class PeerImpl implements Peer {
    private static long id = 0; // might be AtomicLong

    private final ByteBuffer readBuffer;
    private final BlockingQueue<ByteBuffer[]> messagesForSend;
    //private final Map<Long, Consumer<Message>> onResponse;
    private final MessageReader messageReader;
    private Function<SocketChannel, Peer> onAccept;
    private Consumer<Peer> onConnect;
    private Consumer<Peer> onFailure;
    private Consumer<Message> onNewMessage;
    private Consumer<Peer> onWritable;
    private SelectionKey selectionKey;
    private final long peerId;

    @Override
    public void setOnAccept(Function<SocketChannel, Peer> onAccept) {
        this.onAccept = onAccept;
    }

    @Override
    public void setOnConnect(Consumer<Peer> onConnect) {
        this.onConnect = onConnect;
    }

    @Override
    public void setOnNewMessage(Consumer<Message> consumer) {
        this.onNewMessage = consumer;
    }

    @Override
    public void setOnWritable(Consumer<Peer> onWritable) {
        this.onWritable = onWritable;
    }

    @Override
    public Consumer<Peer> getOnWritable() {
        return onWritable;
    }

    @Override
    public void setOnFailure(Consumer<Peer> onFailure) {
        this.onFailure = onFailure;
    }

    public PeerImpl(MessageReader messageReader) {
        this.messageReader = messageReader;
        this.readBuffer = ByteBuffer.allocate(128);
        this.peerId = id++;
        // onResponse = new ConcurrentHashMap<>();
        messagesForSend = new ArrayBlockingQueue<>(128);
    }

    public PeerImpl() {
        this.peerId = id++;
        this.messageReader = null;
        this.readBuffer = null;
        //onResponse = new ConcurrentHashMap<>();
        messagesForSend = new ArrayBlockingQueue<>(128);
    }

    @Override
    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    @Override
    public SelectionKey register(SelectableChannel channel, Selector selector, int ops) throws ClosedChannelException {
        selectionKey = channel.register(selector, ops, this);
        return selectionKey;
    }

    @Override
    public void close() {

    }

    @Override
    public long getPeerId() {
        return peerId;
    }

    @Override
    public BlockingQueue<ByteBuffer[]> getMessagesForSend() {
        return messagesForSend;
    }

    @Override
    public MessageReader getMessageReader() {
        return messageReader;
    }

    @Override
    public Function<SocketChannel, Peer> getOnAccept() {
        return onAccept;
    }

    @Override
    public Consumer<Peer> getOnConnect() {
        return onConnect;
    }

    @Override
    public Consumer<Peer> getOnFailure() {
        return onFailure;
    }

    @Override
    public Consumer<Message> getOnNewMessage() {
        return onNewMessage;
    }
}

