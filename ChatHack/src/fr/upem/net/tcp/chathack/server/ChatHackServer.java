package fr.upem.net.tcp.chathack.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatHackServer {

    static private final int BUFFER_SIZE = 10_000;
    static private final Logger logger = Logger.getLogger(ChatHackServer.class.getName());

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;

    public ChatHackServer(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
    }

    static private class Context {

        final private SelectionKey key;
        final private SocketChannel sc;
        final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final private Queue<ByteBuffer> messageQueue = new LinkedList<>();
        final private ChatHackServer server;
        private boolean inputClosed = false;

        private Context(ChatHackServer server, SelectionKey key){
            this.key = key;
            this.sc = (SocketChannel) key.channel();
            this.server = server;
        }

        /**
         * Process the content of bbin
         *
         * The convention is that bbin is in write-mode before the call
         * to process and after the call
         *
         */
        private void processIn() {
            // TODO
        }

        /**
         * Add a message to the message queue, tries to fill bbOut and updateInterestOps
         *
         * @param msg
         */
        private void queueMessage(ByteBuffer msg) {
            // TODO
        }

        /**
         * Try to fill bbout from the message queue
         *
         */
        private void processOut() {
            // TODO
        }

        /**
         * Update the interestOps of the key looking
         * only at values of the boolean closed and
         * of both ByteBuffers.
         *
         * The convention is that both buffers are in write-mode before the call
         * to updateInterestOps and after the call.
         * Also it is assumed that process has been be called just
         * before updateInterestOps.
         */

        private void updateInterestOps() {
            int interestOps = 0;
            if(!inputClosed && inputBuffer.hasRemaining()) {
                interestOps = interestOps|SelectionKey.OP_READ;
            }
            if(outputBuffer.position() != 0) {
                interestOps = interestOps|SelectionKey.OP_WRITE;
            }
            if(interestOps == 0) {
                silentlyClose();
                return;
            }
            key.interestOps(interestOps);
        }

        private void silentlyClose() {
            try {
                sc.close();
            } catch (IOException e) {
                // ignore exception
            }
        }

        /**
         * Performs the read action on sc
         *
         * The convention is that both buffers are in write-mode before the call
         * to doRead and after the call
         *
         * @throws IOException
         */
        private void doRead() throws IOException {
            if(sc.read(inputBuffer) == -1) {
                logger.log(Level.INFO, "Client has closed the connection");
                inputClosed = true;
            }
            processIn();
            updateInterestOps();
        }

        /**
         * Performs the write action on sc
         *
         * The convention is that both buffers are in write-mode before the call
         * to doWrite and after the call
         *
         * @throws IOException
         */
        private void doWrite() throws IOException {
            outputBuffer.flip();
            sc.write(outputBuffer);
            outputBuffer.compact();
            processOut();
            updateInterestOps();
        }
    }
}
