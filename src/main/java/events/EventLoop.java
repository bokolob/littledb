package events;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Function;

public interface EventLoop {

    void start() throws IOException;

    void addPeer(SelectableChannel channel, Peer peer) throws ClosedChannelException;

    void onNewMessage(Peer peer, Consumer<Message> callback);

    void setOnWritable(Peer peer, Consumer<Peer> consumer);

    void onAccept(Peer peer, Function<SocketChannel, Peer> callback);

    void onConnect(Peer peer, Consumer<Peer> callback);

    void onFailure(Peer peer, Consumer<Peer> callback);

    void onLongPause(Peer peer, Consumer<Peer> callback);

    void sendMessage(Peer peer, Message message);

    void addTimeoutHandler(Consumer<Long> handler);
}
