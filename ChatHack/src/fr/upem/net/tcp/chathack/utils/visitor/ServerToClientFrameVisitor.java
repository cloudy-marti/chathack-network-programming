package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.nio.ByteBuffer;

public class ServerToClientFrameVisitor implements FrameVisitor {

    private static final int BDD_BUFFER_SIZE = 1_024;

    private final ServerToClientContext context;
    private final ChatHackServer server;

    public ServerToClientFrameVisitor(ServerToClientContext context, ChatHackServer server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        BDDServerFrame bddFrame = BDDServerFrame.createBDDServerFrame(context.getId(), frame.getLogin());
        ByteBuffer tmp = ByteBuffer.allocate(BDD_BUFFER_SIZE);
        bddFrame.fillByteBuffer(tmp);
        server.sendRequestToBDD(tmp);
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        BDDServerFrameWithPassword bddFrame = BDDServerFrameWithPassword
                .createBDDServerFrameWithPassword(context.getId(), frame.getLogin(), frame.getPassword());
        ByteBuffer tmp = ByteBuffer.allocate(BDD_BUFFER_SIZE);
        bddFrame.fillByteBuffer(tmp);
        server.sendRequestToBDD(tmp);
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        server.broadcast(frame);
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        // TODO
        // to ask
        //server.privateConnectionFrame(frame);
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(SimpleFrame frame) {
        throw new UnsupportedOperationException("client does not send these kind of frames");
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
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }
}
