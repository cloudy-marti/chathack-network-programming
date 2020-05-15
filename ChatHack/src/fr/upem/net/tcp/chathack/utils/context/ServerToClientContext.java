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

    public void queueMessage(ByteBuffer msg) {
        // TODO
    }

    public void processOut() {
        // TODO
    }

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

    public void doRead() throws IOException {
        if(sc.read(inputBuffer) == -1) {
            //logger.log(Level.INFO, "Client has closed the connection");
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

    @Override
    public void doConnect() {

    }
}
