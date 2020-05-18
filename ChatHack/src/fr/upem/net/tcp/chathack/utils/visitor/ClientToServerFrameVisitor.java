package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;

import java.nio.ByteBuffer;

public class ClientToServerFrameVisitor implements FrameVisitor {

    private final Context context;
    private final ChatHackClient client;
    private static final int BUFFER_SIZE = 10_000;

    public ClientToServerFrameVisitor(Context context, ChatHackClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        switch (frame.getOpcode()) {
            //Un client qui se connecte
            case 04:
                client.getContextPrivateConnection().put(frame.getLogin(), context);
                var queue = client.getWaitingMessage().get(frame.getLogin());
                while (!queue.isEmpty()) {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    var privateMessage = SimpleFrame.createSimpleFrame(21, queue.poll());
                    privateMessage.fillByteBuffer(buffer);
                    context.queueMessage(buffer);
                }
                break;
            case 00:
            case 03:
            default:
                throw new UnsupportedOperationException("The client can't receive this");

        }
    }

    @Override
    public void visit(FilesFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(GlobalMessageFrame frame) {

    }

    @Override
    public void visit(SimpleFrame frame) {

    }

    @Override
    public void visit(LoginPasswordFrame frame) {

    }

    @Override
    public void visit(PrivateConnectionFrame frame) {

    }

    @Override
    public void visit(BDDServerFrame frame) {
        throw new UnsupportedOperationException("client does not interact with BDD server");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("client does not interact with BDD server");
    }
}
