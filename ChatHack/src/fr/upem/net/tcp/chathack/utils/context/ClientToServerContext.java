package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.FrameReader;
import fr.upem.net.tcp.chathack.utils.reader.Reader;
import fr.upem.net.tcp.chathack.utils.visitor.ClientToServerFrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToServerContext implements Context {

    //private static final Logger logger = Logger.getLogger(ChatHackClient.Context.class.getName());

    final private SelectionKey key;
    final private SocketChannel sc;
    final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
    final private FrameReader frameReader = new FrameReader();
    private boolean inputClosed = false;

    private final ClientToServerFrameVisitor frameVisitor = new ClientToServerFrameVisitor(this);

    public ClientToServerContext(SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
    }

    @Override
    public void processIn() {
        FrameReader frameReader = new FrameReader();
        for(;;){
            Reader.ProcessStatus status = frameReader.process(inputBuffer);
            switch (status){
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

    }

    @Override
    public void queueMessage(ByteBuffer msg) {
        queue.add(msg);
        processOut();
        updateInterestOps();
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
            //logger.log(Level.INFO, "connection has been shut down by the server");
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
            //logger.log(Level.INFO, "closed before reading");
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
        if(!sc.finishConnect()){
            return;
        }
        updateInterestOps();
    }
}
