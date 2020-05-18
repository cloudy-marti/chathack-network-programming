package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.visitor.ServerToBDDFrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerToBDDContext implements Context {

    private static final int BUFFER_SIZE = 1_024;
    private static final Logger LOGGER = Logger.getLogger(ServerToBDDContext.class.getName());

    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    private final Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode

    private boolean inputClosed = false;

    private final ServerToBDDFrameVisitor frameVisitor = new ServerToBDDFrameVisitor();

    public ServerToBDDContext(SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
    }

    @Override
    public void processIn() {

    }

    @Override
    public void treatFrame(ChatHackFrame frame) {
        frame.accept(frameVisitor);
    }

    @Override
    public void queueMessage(ByteBuffer msg) {
        queue.add(msg);
        processOut();
        updateInterestOps();
    }

    @Override
    public void processOut() {

    }

    @Override
    public void updateInterestOps() {
        var interestOps=0;
        if (!inputClosed && bbin.hasRemaining()){
            interestOps=interestOps|SelectionKey.OP_READ;
        }
        if (bbout.position()!=0){
            interestOps|=SelectionKey.OP_WRITE;
        }
        if (interestOps==0){
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
        if (sc.read(bbin) == -1) {
            LOGGER.log(Level.INFO, "closed before reading");
            inputClosed = true;
        }
        processIn();
        updateInterestOps();
    }

    @Override
    public void doWrite() throws IOException {
        bbout.flip();
        sc.write(bbout);
        bbout.compact();
        processOut();
        updateInterestOps();
    }

    @Override
    public void doConnect() throws IOException {
        if(!sc.finishConnect()) {
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
    }
}
