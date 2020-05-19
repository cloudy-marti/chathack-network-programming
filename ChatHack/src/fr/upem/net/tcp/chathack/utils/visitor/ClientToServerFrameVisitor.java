package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCode;

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
        throw new UnsupportedOperationException("Connections are not allowed on global chat.");
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        switch (frame.getOpcode()) {
            case 20:
                System.out.println(frame);
                break;
            default:
                throw new UnsupportedOperationException("this is not allowed on global chat.");
        }
    }

    @Override
    public void visit(SimpleFrame frame) {
        switch (frame.getOpcode()) {
            case 10:
            case 11:
                if (client.connected()) {
                    throw new UnsupportedOperationException("The client is already connected.");
                }
                client.setConnected();
                break;
            case 12:
                client.stop();
                break;
            default:
                throw new UnsupportedOperationException("this is not allowed on global chat.");
        }
    }

    @Override
    public void visit(LoginPasswordFrame frame) {

    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        try {
            client.getConnectionRequest().put(frame);
            System.out.println(frame);
        } catch (InterruptedException e) {
            return;
        }
    }

    @Override
    public void visit(BDDServerFrame frame) {
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }

    @Override
    public void visit(BDDServerFrameWithPassword frame) {
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("client does not interact with BDD server");
    }
}
