package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCode;

import java.nio.ByteBuffer;

public class ClientToClientFrameVisitor implements FrameVisitor {
    private final Context context;
    private final ChatHackClient client;
    private static final int BUFFER_SIZE = 10_000;

    public ClientToClientFrameVisitor(Context context, ChatHackClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        switch (frame.getOpcode()) {
            case 04:
                client.getContextPrivateConnection().put(frame.getLogin(), context);
                var queue = client.getWaitingMessage().get(frame.getLogin());
                while (!queue.isEmpty()) {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    var privateMessage = SimpleFrame.createSimpleFrame(OpCode.PRIVATE_MESSAGE.getOpCode(), queue.poll());
                    privateMessage.fillByteBuffer(buffer);
                    context.queueMessage(buffer);
                }
                break;
            default:
                throw new UnsupportedOperationException("The client can't receive this");
        }

    }

    @Override
    public void visit(FileFrame frame) {

    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(SimpleFrame frame) {
        switch (frame.getOpcode()) {
            case 21:
                System.out.println(frame);
                break;
            default:
                throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
        }
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(BDDServerFrame frame) {
        throw new UnsupportedOperationException("BDD frames between clients are not allowed");
    }

    @Override
    public void visit(BDDServerFrameWithPassword frame) {
        throw new UnsupportedOperationException("BDD frames between clients are not allowed");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("BDD frames between clients are not allowed");
    }
}
