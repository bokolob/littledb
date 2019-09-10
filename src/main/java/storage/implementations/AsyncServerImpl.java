package storage.implementations;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

import events.EventLoop;
import events.Message;
import events.Peer;
import events.implementations.MessageImpl;
import events.implementations.MessageReaderImpl;
import events.implementations.PeerImpl;
import storage.CommandParser;

public class AsyncServerImpl {
    int port;
    CommandParser commandParser;
    private final EventLoop eventLoop;

    public AsyncServerImpl(int port, CommandParser commandParser, EventLoop eventLoop) {
        this.port = port;
        this.commandParser = commandParser;
        this.eventLoop = eventLoop;
    }

    private Peer handleAccept(EventLoop eventLoop, SocketChannel channel) {
        Peer p = new PeerImpl(new MessageReaderImpl());
        try {
            eventLoop.addPeer(channel, p);
            eventLoop.onNewMessage(p, msg -> {
                //sendResponse(eventLoop, p, ByteBuffer.wrap(("Response to "+msg.getMessageId()).getBytes()),msg
                // .getMessageId());
                try {
                    commandParser.processCommand(new String(msg.payload()))
                            .thenAccept(b ->
                                    sendResponse(eventLoop, p, b, msg.getMessageId()))
                            .exceptionally(throwable -> {
                                System.err.println(throwable.getMessage());
                                return null;
                            }).get();

                } catch (IOException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
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

    public void run() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);

        InetSocketAddress inetSocketAddress = new InetSocketAddress((InetAddress) null, port);
        serverSocket.bind(inetSocketAddress);

        Peer serverPeer = new PeerImpl();

        eventLoop.addPeer(serverSocket, serverPeer);
        eventLoop.onAccept(serverPeer, channel -> handleAccept(eventLoop, channel));

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
