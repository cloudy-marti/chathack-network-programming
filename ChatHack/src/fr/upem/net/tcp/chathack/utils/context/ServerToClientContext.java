package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.frame.FrameReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.visitor.ServerToClientFrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerToClientContext implements Context {

    private static final Logger LOGGER = Logger.getLogger(ServerToClientContext.class.getName());

    final private SelectionKey key;
    final private SocketChannel sc;

    final private long id;
    private String login;
    private String password = "";

    final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    final private Queue<ByteBuffer> messageQueue = new LinkedList<>();

    final private ChatHackServer server;

    private boolean inputClosed = false;
    private boolean accepted = false;

    private final ServerToClientFrameVisitor frameVisitor;

    private ServerToClientContext privateClientConnection;

    public ServerToClientContext(ChatHackServer server, SelectionKey key, long id){
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.server = server;
        this.frameVisitor = new ServerToClientFrameVisitor(this, server);
        this.id = id;
    }

    public void processIn() {
        FrameReader frameReader = new FrameReader();
        for(;;) {
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

    public void treatFrame(ChatHackFrame frame) {
        LOGGER.log(Level.INFO, "Accepting frame with opcode " + frame.getOpcode());
        frame.accept(frameVisitor);
    }

    @Override
    public void queueMessage(ByteBuffer msg) {
        messageQueue.add(msg);
        processOut();
        updateInterestOps();
    }

    public void processOut() {
        while (!messageQueue.isEmpty()) {
            ByteBuffer tmp = messageQueue.peek();
            if(tmp.remaining() <= outputBuffer.remaining()) {
                messageQueue.remove();
                outputBuffer.put(tmp);
                tmp.flip();
            } else {
                return;
            }
        }
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
            LOGGER.log(Level.INFO, "Client has closed the connection");
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
        throw new UnsupportedOperationException();
    }

    public long getId() {
        return this.id;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setLoginAndPassword(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void setPrivateClientConnection(ServerToClientContext privateClientConnection) {
        this.privateClientConnection = privateClientConnection;
    }

    public ServerToClientContext getPrivateClientConnection() {
        return this.privateClientConnection;
    }
}
