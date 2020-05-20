package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.nio.ByteBuffer;

public class ServerToBDDFrameVisitor implements FrameVisitor {

    private final ServerToBDDContext context;
    private final ChatHackServer server;

    public ServerToBDDFrameVisitor(ServerToBDDContext context, ChatHackServer server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        long id = bddServerResponseFrame.getId();
        ServerToClientContext client = server.getClientById(id);
        SimpleFrame responseConnect;
        if(bddServerResponseFrame.isValid()) {
            responseConnect = SimpleFrame.createSimpleFrame(10, "ok");
        } else {
            responseConnect = SimpleFrame.createSimpleFrame(12, "login or password invalid");
        }
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        responseConnect.fillByteBuffer(tmp);
        client.queueMessage(tmp);
    }

    @Override
    public void visit(ConnectionFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(SimpleFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        throw new UnsupportedOperationException();
    }
}
