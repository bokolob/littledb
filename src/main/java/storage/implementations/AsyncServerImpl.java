package storage.implementations;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import actors.ActorMessageRouter;
import actors.implementations.BaseActorImpl;
import events.EventLoop;
import events.Message;
import events.Peer;
import events.implementations.MessageImpl;
import events.implementations.MessageReaderImpl;
import events.implementations.PeerImpl;
import storage.implementations.commands.messages.ClientRequest;

public class AsyncServerImpl extends BaseActorImpl {
    private final int port;
    private final EventLoop eventLoop;
    private final Map<Long, Peer> peerMap;
    private final ActorMessageRouter router;

    public AsyncServerImpl(int threadCount, int port, EventLoop eventLoop,
                           ActorMessageRouter router) {
        super(threadCount, router);
        this.port = port;
        this.eventLoop = eventLoop;
        this.router = router;
        peerMap = new ConcurrentHashMap<>();
    }

    private Peer handleAccept(EventLoop eventLoop, SocketChannel channel) {
        Peer p = new PeerImpl(new MessageReaderImpl());
        peerMap.put(p.getPeerId(), p);

        try {
            eventLoop.addPeer(channel, p);
            eventLoop.onNewMessage(p, msg -> {

                ClientRequest request = new ClientRequest(this, new String(msg.payload()),
                        v -> sendResponse(eventLoop, p, v.map(String::getBytes).orElseGet(() -> new byte[]{}),
                                msg.getMessageId())
                );

                router.sendRequest(request);
            });
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }


    private void sendResponse(EventLoop eventLoop, Peer to, byte[] response, long id) {
        Message message = new MessageImpl(id, response, Message.MessageType.RESPONSE);
        eventLoop.sendMessage(to, message);
    }

    private void init() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);

        InetSocketAddress inetSocketAddress = new InetSocketAddress((InetAddress) null, port);
        serverSocket.bind(inetSocketAddress);

        Peer serverPeer = new PeerImpl();

        eventLoop.addPeer(serverSocket, serverPeer);
        eventLoop.onAccept(serverPeer, channel -> handleAccept(eventLoop, channel));
    }

    @Override
    public void run() {

        try {
            init();
            super.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
