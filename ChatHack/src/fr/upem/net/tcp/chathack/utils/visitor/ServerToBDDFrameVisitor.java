package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerToBDDFrameVisitor implements FrameVisitor {

    private final static Logger LOGGER = Logger.getLogger(ServerToBDDFrameVisitor.class.getName());

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
        if(bddServerResponseFrame.isPresentOnBDD()) {
            if(server.getClientById(id).getPassword().isEmpty()) {
                LOGGER.log(Level.INFO, "Login already in use, cannot be taken");
                responseConnect = SimpleFrame.createSimpleFrame(12,
                        "Login already in use, cannot be taken");
            } else {
                LOGGER.log(Level.INFO, "Connection with login and password accepted, welcome to ChatHack");
                responseConnect = SimpleFrame.createSimpleFrame(11,
                        "Connection with login and password accepted, welcome to ChatHack");
            }
        } else {
            if(server.getClientById(id).getPassword().isEmpty()) {
                LOGGER.log(Level.INFO, "Login available, welcome to ChatHack");
                responseConnect = SimpleFrame.createSimpleFrame(10,
                        "Login available, welcome to ChatHack");
            } else {
                LOGGER.log(Level.INFO, "Login and password not accepted");
                responseConnect = SimpleFrame.createSimpleFrame(12, "Login and password not accepted");
            }
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
