package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.reader.frame.FrameReader;
import fr.upem.net.tcp.chathack.utils.reader.frame.serverbdd.BDDFrameReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
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

    private final ChatHackServer server;

    private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    private final Queue<ByteBuffer> messageQueue = new LinkedList<>(); // buffers read-mode

    private boolean inputClosed = false;

    private final ServerToBDDFrameVisitor frameVisitor;

    public ServerToBDDContext(SelectionKey key, ChatHackServer server) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.server = server;
        this.frameVisitor = new ServerToBDDFrameVisitor(this, server);
    }

    @Override
    public void processIn() {
        BDDFrameReader frameReader = new BDDFrameReader();
        for(;;) {
            Reader.ProcessStatus status = frameReader.process(bbin);
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
        frame.accept(frameVisitor);
    }

    @Override
    public void queueMessage(ByteBuffer msg) {
        messageQueue.add(msg);
        processOut();
        updateInterestOps();
    }

    @Override
    public void processOut() {
        while (!messageQueue.isEmpty()) {
            ByteBuffer tmp = messageQueue.peek();
            if(tmp.remaining() <= bbout.remaining()) {
                messageQueue.remove();
                bbout.put(tmp);
            } else {
                return;
            }
        }
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
        LOGGER.log(Level.INFO, "non monsieur :(");
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
