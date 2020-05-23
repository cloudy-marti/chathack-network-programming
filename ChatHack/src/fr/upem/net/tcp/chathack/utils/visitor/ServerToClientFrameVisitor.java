package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

public class ServerToClientFrameVisitor implements FrameVisitor {

    private final static Logger LOGGER = Logger.getLogger(ServerToClientFrameVisitor.class.getName());

    private static final int BDD_BUFFER_SIZE = 1_024;

    private final ServerToClientContext context;
    private final ChatHackServer server;

    public ServerToClientFrameVisitor(ServerToClientContext context, ChatHackServer server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        LOGGER.log(Level.INFO, "Visiting ConnectionFrame from Client Id : " + context.getId());
        server.saveClientLogin(context.getId(), frame.getLogin());
        context.setLoginAndPassword(frame.getLogin(), "");
        BDDServerFrame bddFrame = BDDServerFrame.createBDDServerFrame(context.getId(), frame.getLogin());
        ByteBuffer tmp = ByteBuffer.allocate(BDD_BUFFER_SIZE);
        bddFrame.fillByteBuffer(tmp);
        server.sendRequestToBDD(tmp);
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        LOGGER.log(Level.INFO, "Visiting LoginPasswordFrame from Client Id : " + context.getId());
        if(server.getClientByLogin(frame.getLogin()) != null) {
            SimpleFrame responseConnect = SimpleFrame.createSimpleFrame(12,
                    "Registered user already connected");
            ByteBuffer tmp = ByteBuffer.allocate(1_024);
            responseConnect.fillByteBuffer(tmp);
            context.queueMessage(tmp);
            server.removeClient(context.getId());
            return;
        }
        server.saveClientLogin(context.getId(), frame.getLogin());
        context.setLoginAndPassword(frame.getLogin(), frame.getPassword());
        BDDServerFrameWithPassword bddFrame = BDDServerFrameWithPassword
                .createBDDServerFrameWithPassword(context.getId(), frame.getLogin(), frame.getPassword());
        ByteBuffer tmp = ByteBuffer.allocate(BDD_BUFFER_SIZE);
        bddFrame.fillByteBuffer(tmp);
        server.sendRequestToBDD(tmp);
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        LOGGER.log(Level.INFO, "Visiting GlobalMessageFrame");
        server.broadcast(frame);
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        LOGGER.log(Level.INFO, "Visiting PrivateConnectionFrame coming from client Id : " + context.getId());
        String dest = frame.getLogin();
        ServerToClientContext destClient = server.getClientByLogin(dest);
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        if(destClient == null) {
            SimpleFrame response = SimpleFrame.createSimpleFrame(30, "dest login not recognised");
            response.fillByteBuffer(tmp);
            context.queueMessage(tmp);
            return;
        }
        destClient.setPrivateClientConnection(context);
        frame.fillByteBuffer(tmp);
        destClient.queueMessage(tmp);
    }

    @Override
    public void visit(PrivateConnectionResponseFrame frame) {

    }

    @Override
    public void visit(SimpleFrame frame) {
        int opcode = frame.getOpcode();
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        if(opcode == DISCONNECTION_REQUEST) { // disconnection request
            SimpleFrame responseDisconnect = SimpleFrame.createSimpleFrame(15, "Disconnection OK");
            responseDisconnect.fillByteBuffer(tmp);
            context.queueMessage(tmp);
            server.removeClient(context.getId());
            context.silentlyClose();
        } else if(opcode == PRIVATE_CONNECTION_OK || opcode == PRIVATE_CONNECTION_KO) { // private connection response
            LOGGER.log(Level.INFO, "client has responded to private connection request");
            ServerToClientContext dest = context.getPrivateClientConnection();
            context.setPrivateClientConnection(null);
            frame.fillByteBuffer(tmp);
            dest.queueMessage(tmp);
        } else {
            throw new UnsupportedOperationException("client does not send these kind of frames");
        }
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }
}
