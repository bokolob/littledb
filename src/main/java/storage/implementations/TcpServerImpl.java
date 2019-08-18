package storage.implementations;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import storage.CommandParser;

public class TcpServerImpl {
    CommandParser commandParser;

    public TcpServerImpl(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    private static class ErrorSender implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        ByteBuffer errBuffer;

        public ErrorSender(String message) {
            this.errBuffer = ByteBuffer.wrap(message.getBytes());
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (result != -1 && result < errBuffer.limit()) {
                channel.write(errBuffer, channel, this);
            }

            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            errBuffer.clear();
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

        }
    }

    private static class ResponseWriter implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        ConnectionHandler readHandler;
        ByteBuffer buffer;

        public ResponseWriter(ConnectionHandler readHandler, ByteBuffer buffer) {
            this.readHandler = readHandler;
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (result != -1) {
                if (buffer.position() < buffer.limit()) {
                    channel.write(buffer, channel, this);
                } else {
                    readHandler.completed(0, channel);
                }

                return;
            }

            System.err.println("Connection lost");

            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

        }
    }

    private static class ConnectionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        ByteBuffer byteBuffer;
        StringBuffer stringBuffer;
        CommandParser commandParser;

        public ByteBuffer buffer() {
            return byteBuffer;
        }

        public ConnectionHandler(CommandParser commandParser) {
            this.byteBuffer = ByteBuffer.allocate(128);
            this.stringBuffer = new StringBuffer();
            this.commandParser = commandParser;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (result == -1) {
                System.err.println("Connection failed..");
                finish(channel);
                return;
            }

            byteBuffer.flip();

            final int len = byteBuffer.limit();

            for (int i = 0; i < len; i++) {
                byte b = byteBuffer.get();

                if (b == '\n') {
                    try {
                        ByteBuffer responseBuffer =
                                ByteBuffer.wrap(commandParser.processCommand(stringBuffer.toString()));
                        ResponseWriter responseWriter = new ResponseWriter(this, responseBuffer);
                        byteBuffer = byteBuffer.slice();
                        stringBuffer = new StringBuffer();
                        channel.write(responseWriter.buffer, channel, responseWriter);
                        //channel.write(responseBuffer).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                } else {
                    stringBuffer.appendCodePoint(b);
                }
            }

            byteBuffer.clear();

            if (byteBuffer.remaining() == 0) {
                byteBuffer = ByteBuffer.allocate(128);
            }

            channel.read(byteBuffer, channel, this);
        }

        private void sendErrorString(AsynchronousSocketChannel channel, String err) {
            ErrorSender errorSender = new ErrorSender(err);
            channel.write(errorSender.errBuffer, channel, errorSender);
        }

        private void finish(AsynchronousSocketChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteBuffer.clear();
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

        }
    }

    private static class AcceptHandler implements
            CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>
    {
        CommandParser commandParser;

        public AcceptHandler(CommandParser commandParser) {
            this.commandParser = commandParser;
        }

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, AsynchronousServerSocketChannel serverChannel) {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }

            if ((clientChannel != null) && (clientChannel.isOpen())) {
                ConnectionHandler handler = new ConnectionHandler(commandParser);
                clientChannel.read(handler.buffer(), clientChannel, handler);
            }

        }

        @Override
        public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {

        }

    }

    public void run() throws IOException {
        AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();

        InetSocketAddress hostAddress = new InetSocketAddress(4999);
        serverChannel.bind(hostAddress);
        serverChannel.accept(serverChannel, new AcceptHandler(commandParser));

        while (true) {
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
