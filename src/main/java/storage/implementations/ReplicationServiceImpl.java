package storage.implementations;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import events.EventLoop;
import events.Message;
import events.Peer;
import events.implementations.MessageImpl;
import events.implementations.PeerImpl;
import row.Key;
import row.Value;
import storage.ReplicationService;

public class ReplicationServiceImpl implements ReplicationService {

    private final Map<String, NodeDescription> nodeDescriptionMap;
    private final EventLoop eventLoop;
    private final AtomicLong messageId;

    private static final int PING_PERIOD = 5;
    private static final int PING_TIMEOUT = 20;

    public ReplicationServiceImpl(
            Map<String, NodeDescription> nodeDescriptionMap, EventLoop eventLoop)
            throws IOException
    {
        this.nodeDescriptionMap = nodeDescriptionMap;
        this.eventLoop = eventLoop;
        messageId = new AtomicLong(1L);

        this.eventLoop.addTimeoutHandler(this::onTimeout);

        for (NodeDescription nodeDescription : nodeDescriptionMap.values()) {
            nodeDescription.connect(eventLoop);
        }
    }

    public void onTimeout(long v) {
        System.out.println("Time!");
    }

    public static class NodeDescription {
        private final SocketAddress socketAddress;
        private Peer peer;
        private long lastPingTime = 0;

        public NodeDescription(String host, int port) {
            this.socketAddress = new InetSocketAddress(host, port);
            this.peer = new PeerImpl();
        }

        public void connect(EventLoop eventLoop) throws IOException {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            try {
                channel.connect(socketAddress);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            eventLoop.onFailure(peer, p -> System.err.println(p + " failed"));
            eventLoop.addPeer(channel, this.peer);
            eventLoop.onConnect(peer, p -> lastPingTime = System.currentTimeMillis());
            eventLoop.onNewMessage(peer, message -> lastPingTime = System.currentTimeMillis());
        }

        public Peer getPeer() {
            return peer;
        }
    }

    @Override
    public void replicate(Key key, Value value) {

        for (NodeDescription nodeDescription : nodeDescriptionMap.values()) {
            if (!nodeDescription.getPeer().getSelectionKey().isValid()) {
                continue;
            }

            eventLoop.sendMessage(nodeDescription.getPeer(),
                    new MessageImpl(messageId.getAndIncrement(), value.asBytes(), Message.MessageType.COMMAND));
        }

    }
}
