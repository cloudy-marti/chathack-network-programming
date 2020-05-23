package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

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
        GlobalMessageFrame welcomeMessage;
        if(bddServerResponseFrame.isPresentOnBDD()) {
            if(server.getClientById(id).getPassword().isEmpty()) {
                LOGGER.log(Level.INFO, "Login already in use, cannot be taken");
                responseConnect = SimpleFrame.createSimpleFrame(CONNECTION_KO,
                        "Login already in use, cannot be taken");
                ByteBuffer tmp = ByteBuffer.allocate(1_024);
                responseConnect.fillByteBuffer(tmp);
                client.queueMessage(tmp);
                server.removeClient(id);
                return;
            } else {
                LOGGER.log(Level.INFO, "Connection with login and password accepted, welcome to ChatHack");
                responseConnect = SimpleFrame.createSimpleFrame(CONNECTION_WITH_LOGIN_AND_PASSWORD_OK,
                        "Connection with login and password accepted, welcome to ChatHack");
                welcomeMessage = GlobalMessageFrame.createGlobalMessageFrame(GLOBAL_MESSAGE, "",
                        client.getLogin() + " has entered the chat");
                server.broadcast(welcomeMessage);
            }
        } else {
            if(server.getClientById(id).getPassword().isEmpty()) {
                LOGGER.log(Level.INFO, "Login available, welcome to ChatHack");
                responseConnect = SimpleFrame.createSimpleFrame(CONNECTION_WITH_LOGIN_OK,
                        "Login available, welcome to ChatHack");
                welcomeMessage = GlobalMessageFrame.createGlobalMessageFrame(GLOBAL_MESSAGE, "",
                        client.getLogin() + " has entered the chat");
                server.broadcast(welcomeMessage);
            } else {
                LOGGER.log(Level.INFO, "Login and password not accepted");
                responseConnect = SimpleFrame
                        .createSimpleFrame(CONNECTION_KO, "Login and password not accepted");
                ByteBuffer tmp = ByteBuffer.allocate(1_024);
                responseConnect.fillByteBuffer(tmp);
                client.queueMessage(tmp);
                server.removeClient(id);
            }
        }
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        responseConnect.fillByteBuffer(tmp);
        client.queueMessage(tmp);
    }

    @Override
    public void visit(PrivateConnectionResponseFrame frame) {
        throw new UnsupportedOperationException();
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
