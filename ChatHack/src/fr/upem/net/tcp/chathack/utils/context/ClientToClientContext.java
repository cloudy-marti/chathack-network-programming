package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.frame.FrameReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.visitor.ClientToClientFrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Context of the client-client connection known by each client
 */
public class ClientToClientContext implements Context {
    private static final Logger LOGGER = Logger.getLogger(ClientToClientContext.class.getName());

    final private SelectionKey key;
    final private SocketChannel sc;
    final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
    private boolean inputClosed = false;
    private final ChatHackClient client;
    private String login = null;

    private final ClientToClientFrameVisitor frameVisitor;

    public ClientToClientContext(SelectionKey key, ChatHackClient client) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(client);
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.client = client;
        this.frameVisitor = new ClientToClientFrameVisitor(this, client);
    }

    @Override
    public void processIn() {
        FrameReader frameReader = new FrameReader();
        for (; ; ) {
            Reader.ProcessStatus status = frameReader.process(inputBuffer);
            switch (status) {
                case ERROR:
                    silentlyClose();
                    return;
                case REFILL:
                    return;
                case DONE:
                    ChatHackFrame frame = frameReader.get();
                    frameReader.reset();
                    treatFrame(frame);
                    break;
            }
        }
    }

    @Override
    public void treatFrame(ChatHackFrame frame) {
        Objects.requireNonNull(frame);
        frame.accept(frameVisitor);
    }

    @Override
    public void queueMessage(ByteBuffer msg) {
        Objects.requireNonNull(msg);
        queue.add(msg);
        processOut();
        updateInterestOps();
    }


    public void queueMessageWithoutUpdateInterestOps(ByteBuffer msg) {
        Objects.requireNonNull(msg);
        queue.add(msg);
        processOut();
    }

    @Override
    public void processOut() {
        while (!queue.isEmpty()) {
            var bb = queue.peek();
            if (bb.remaining() <= outputBuffer.remaining()) {
                queue.remove();
                outputBuffer.put(bb);
            } else {
                return;
            }
        }
    }

    @Override
    public void updateInterestOps() {
        var interestOps = 0;
        if (!inputClosed && inputBuffer.hasRemaining()) {
            interestOps = interestOps | SelectionKey.OP_READ;
        }
        if (outputBuffer.position() != 0) {
            interestOps |= SelectionKey.OP_WRITE;
        }
        if (interestOps == 0) {
            silentlyClose();
            return;
        }
        try {
            key.interestOps(interestOps);
        } catch (CancelledKeyException kE) {
            LOGGER.log(Level.INFO, "connection has been shut down by the server");
            silentlyClose();
        }
    }

    @Override
    public void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    @Override
    public void doRead() throws IOException {
        if (sc.read(inputBuffer) == -1) {
            LOGGER.log(Level.INFO, "closed before reading");
            inputClosed = true;
        }
        processIn();
        updateInterestOps();
    }

    @Override
    public void doWrite() throws IOException {
        outputBuffer.flip();
        sc.write(outputBuffer);
        outputBuffer.compact();
        processOut();
        updateInterestOps();
    }

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            return;
        }
        updateInterestOps();
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
