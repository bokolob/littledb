package storage.implementations;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import storage.CommandParser;
import storage.AsyncWriteCallback;

public class AsyncServerImpl {
    int port;
    CommandParser commandParser;

    public AsyncServerImpl(int port, CommandParser commandParser) {
        this.port = port;
        this.commandParser = commandParser;
    }

    private static class ValueReader implements AsyncWriteCallback {

        Selector selector;
        Handler handler;
        boolean valid = true;

        public ValueReader(Selector selector, Handler handler) {
            this.selector = selector;
            this.handler = handler;
        }

        @Override
        public void onValueBlockRead(Integer size, ByteBuffer buffer) throws ClosedChannelException {
            int ops;

            if (!valid) {
                throw new IllegalStateException("Trying to call invalid callback!");
            }

            if (size == -1) {
                handler.writeBuffer = null;
                handler.state = Handler.State.READING;
                ops = SelectionKey.OP_READ;
                valid = false;
            } else {
                handler.writeBuffer = buffer;
                ops = SelectionKey.OP_WRITE;
            }

            handler.channel().register(selector, ops, handler);
        }
    }

    private static class Handler {

        enum State {
            READING,
            RESPONDING,
            CLOSED
        }

        SocketChannel channel;
        ByteBuffer readBuffer;
        ByteBuffer writeBuffer;
        StringBuffer stringBuffer;
        CommandParser commandParser;
        State state = State.READING;

        public Handler(SocketChannel channel, CommandParser commandParser) {
            this.channel = channel;

            readBuffer = ByteBuffer.allocate(128);
            readBuffer.limit(0);
            stringBuffer = new StringBuffer();
            this.commandParser = commandParser;
        }

        public SocketChannel channel() {
            return channel;
        }

        public int defaultOps() {
            return SelectionKey.OP_READ;
        }

        private void processNewData(SelectionKey selectionKey) {
            state = State.READING;
            final int len = readBuffer.limit();

            for (int i = readBuffer.position(); i < len; i++) {
                byte b = readBuffer.get();

                if (b == '\n') {
                    try {
                        ValueReader valueReader = new ValueReader(selectionKey.selector(), this);

                        state = State.RESPONDING;
                        selectionKey.cancel();

                        commandParser.processCommand(stringBuffer.toString(), valueReader);
                        stringBuffer = new StringBuffer();

                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    stringBuffer.appendCodePoint(b);
                }
            }

        }

        public int process(SelectionKey selectionKey) throws IOException {
            int rc;

            switch (state) {
                case READING:
                    if (readBuffer.remaining() == 0) {
                        readBuffer.clear();
                        rc = channel.read(readBuffer);

                        if (rc == -1) {
                            state = State.CLOSED;
                            return -1;
                        }

                        readBuffer.flip();
                    }
                    processNewData(selectionKey);
                    break;
                case RESPONDING:
                    rc = channel.write(writeBuffer);

                    if (rc == -1) {
                        state = State.CLOSED;
                        return -1;
                    }

                    if (writeBuffer.remaining() == 0) {
                        writeBuffer = null;
                        state = State.READING;
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        processNewData(selectionKey); // call expicitly to process remaining data in the buffer
                    }

                    break;
                default:
                    throw new IllegalStateException("Unhandled state " + state);
            }

            return 1;
        }
    }

    public void run() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress((InetAddress) null, port);
        serverSocket.bind(inetSocketAddress);

        Thread[] threads = new Thread[4];

        for (int i = 0; i < threads.length; i++) {
            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            threads[i] = new Thread(() -> {
                try {
                    eventLoop(selector);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

            threads[i].setDaemon(false);
            threads[i].start();
        }

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void eventLoop(Selector selector) throws IOException {

        while (true) {

            if (selector.select(100) == 0) {
                //on time-out
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            for (SelectionKey selectionKey : selectedKeys) {

                if (selectionKey.isAcceptable()) {
                    SocketChannel clientChannel = ((ServerSocketChannel) selectionKey.channel()).accept();

                    if (clientChannel == null) {
                        continue;
                    }

                    clientChannel.configureBlocking(false);
                    Handler handler = new Handler(clientChannel, commandParser);
                    SelectionKey clientKey = clientChannel.register(selector, handler.defaultOps());
                    clientKey.attach(handler);
                } else {
                    Handler handler = (Handler) selectionKey.attachment();
                    if (handler.process(selectionKey) == -1) {
                        handler.channel.close();
                    }
                }
            }

        }


    }

}
