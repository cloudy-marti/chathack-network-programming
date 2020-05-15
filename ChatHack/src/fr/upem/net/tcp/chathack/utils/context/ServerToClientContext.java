package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.FrameReader;
import fr.upem.net.tcp.chathack.utils.reader.Reader;
import fr.upem.net.tcp.chathack.utils.visitor.ServerToClientFrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

public class ServerToClientContext implements Context {
    final private SelectionKey key;
    final private SocketChannel sc;
    final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private Queue<ByteBuffer> messageQueue = new LinkedList<>();
    final private ChatHackServer server;
    private boolean inputClosed = false;

    private final ServerToClientFrameVisitor frameVisitor;

    private String login;
    private String password;

    public ServerToClientContext(ChatHackServer server, SelectionKey key){
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.server = server;
        this.frameVisitor = new ServerToClientFrameVisitor(this, server);
    }

    /**
     * Process the content of bbin
     *
     * The convention is that bbin is in write-mode before the call
     * to process and after the call
     *
     */
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

    public void treatFrame(ChatHackFrame frame) {
        frame.accept(frameVisitor);
    }

    /**
     * Add a message to the message queue, tries to fill bbOut and updateInterestOps
     *
     * @param msg
     */
    public void queueMessage(ByteBuffer msg) {
        // TODO
    }

    /**
     * Try to fill bbout from the message queue
     *
     */
    public void processOut() {
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

    public void updateInterestOps() {
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

    public void silentlyClose() {
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
    public void doRead() throws IOException {
        if(sc.read(inputBuffer) == -1) {
            //logger.log(Level.INFO, "Client has closed the connection");
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
    @Override
    public void doWrite() throws IOException {
        outputBuffer.flip();
        sc.write(outputBuffer);
        outputBuffer.compact();
        processOut();
        updateInterestOps();
    }

    @Override
    public void doConnect() {

    }
}
