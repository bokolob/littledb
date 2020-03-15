package events.implementations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import events.EventLoop;
import events.Message;
import events.MessageWriter;
import events.Peer;

public class EventLoopImpl implements EventLoop {
    private final Selector selector;
    private final MessageWriter messageWriter;
    private final List<Consumer<Long>> timeoutHandlers;
    public static final long TIME_SLICE = 100;

    public EventLoopImpl() throws IOException {
        selector = Selector.open();
        messageWriter = new MessageWriterImpl();
        timeoutHandlers = new CopyOnWriteArrayList<>();
    }

    private static class Loop implements Runnable {
        private final Selector selector;
        private long lastIterationTime;
        private final List<Consumer<Long>> timeoutHandlers;

        public Loop(Selector selector, List<Consumer<Long>> timeoutHandlers) {
            this.selector = selector;
            this.timeoutHandlers = timeoutHandlers;
        }

        @Override
        public void run() {
            lastIterationTime = System.currentTimeMillis();
            while (true) {
                try {
                    iteration();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void iteration() throws IOException {
            selector.select(TIME_SLICE);

            if (System.currentTimeMillis() - lastIterationTime >= TIME_SLICE) {
                lastIterationTime = System.currentTimeMillis();
                runTimeoutHandlers(System.currentTimeMillis() );
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            for (SelectionKey selectionKey : selectedKeys) {

                if (!selectionKey.isValid()) {
                    continue;
                }

                Peer peerData = (Peer) selectionKey.attachment();

                if (selectionKey.isAcceptable()) {
                    SocketChannel clientChannel = ((ServerSocketChannel) selectionKey.channel()).accept();

                    if (clientChannel == null) {
                        continue;
                    }

                    clientChannel.configureBlocking(false);
                    peerData.getOnAccept().apply(clientChannel);

                }

                if (selectionKey.isConnectable()) {

                    try {
                        ((SocketChannel) selectionKey.channel()).finishConnect();
                        selectionKey.interestOpsAnd(~SelectionKey.OP_CONNECT);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        selectionKey.channel().close();
                        selectionKey.cancel();
                        peerData.getOnFailure().accept(peerData);
                        continue;
                    }
                }

                if (selectionKey.isReadable()) {
                    int rc = ((SocketChannel) selectionKey.channel()).read(peerData.getReadBuffer());

                    if (rc == -1) {
                        //TODO
                        selectionKey.channel().close();
                        selectionKey.cancel();
                        //NPE
                        peerData.getOnFailure().accept(peerData);
                        peerData.close();
                        continue;
                    }

                    peerData.getMessageReader().consume(peerData.getReadBuffer());

                    peerData.getReadBuffer().clear();

                    while (peerData.getMessageReader().hasNext()) {
                        Message msg = peerData.getMessageReader().next();
                        peerData.getOnNewMessage().accept(msg);
                    }

                }

                if (selectionKey.isWritable()) {
                    ByteBuffer[] byteBuffer = peerData.getMessagesForSend().peek();

                    if (byteBuffer != null) {
                        ((SocketChannel) selectionKey.channel()).write(byteBuffer);

                        long remaining = remaining(byteBuffer);

                        if (remaining == 0) {
                            peerData.getMessagesForSend().poll();
                        }
                    }

                    if (peerData.getMessagesForSend().size() == 0) {
                        selectionKey.interestOpsAnd(~SelectionKey.OP_WRITE);
                    }

                    if (peerData.getMessagesForSend().size() > 0) {
                        selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
                    }

                    if (peerData.getOnWritable() != null) {
                        peerData.getOnWritable().accept(peerData);
                    }

                }

            }


        }

        private void runTimeoutHandlers(long currentTime) {
            for (Consumer<Long> consumer: timeoutHandlers) {
                consumer.accept(currentTime);
            }
        }

        private long remaining(ByteBuffer[] byteBuffers) {
            long result = 0;

            for (ByteBuffer b : byteBuffers) {
                result += b.remaining();
            }

            return result;
        }

    }


    @Override
    public void start() throws IOException {
        Thread thr = new Thread(new Loop(selector, timeoutHandlers), "EventLoop");
        thr.setDaemon(true);
        thr.start();
    }

    @Override
    public void addPeer(SelectableChannel channel, Peer peer) throws ClosedChannelException {
        peer.register(channel, selector, 0);
    }

    @Override
    public void onNewMessage(Peer peer, Consumer<Message> callback) {
        peer.setOnNewMessage(callback);
        peer.getSelectionKey().interestOpsOr(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void setOnWritable(Peer peer, Consumer<Peer> consumer) {
        peer.setOnWritable(consumer);
        peer.getSelectionKey().interestOpsOr(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    @Override
    public void onAccept(Peer peer, Function<SocketChannel, Peer> callback) {
        SelectionKey selectionKey = peer.getSelectionKey();
        peer.setOnAccept(callback);
        selectionKey.interestOpsOr(SelectionKey.OP_ACCEPT);
        selector.wakeup();
    }

    @Override
    public void onConnect(Peer peer, Consumer<Peer> callback) {
        SelectionKey selectionKey = peer.getSelectionKey();
        selectionKey.interestOpsOr(SelectionKey.OP_CONNECT);
        peer.setOnConnect(callback);
    }

    @Override
    public void onFailure(Peer peer, Consumer<Peer> callback) {
        peer.setOnFailure(callback);
    }

    @Override
    public void onLongPause(Peer peer, Consumer<Peer> callback) {

    }

    @Override
    public void sendMessage(Peer peer, Message message) {
        peer.getMessagesForSend().add(messageWriter.asByteBufferSequence(message));

        SelectionKey selectionKey = peer.getSelectionKey();
//RACE ?
        if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
            selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    @Override
    public void addTimeoutHandler(Consumer<Long> handler) {
        timeoutHandlers.add(handler);
    }
}
