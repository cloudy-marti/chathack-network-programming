package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerResponseFrame;

import java.nio.ByteBuffer;
import java.util.Objects;
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
        Objects.requireNonNull(frame);
        int opcode = frame.getOpcode();
        if(opcode == CONNECTION_WITH_LOGIN) {
            LOGGER.log(Level.INFO, "Visiting ConnectionFrame from Client Id : " + context.getId());
            ServerToClientContext destClient = server.getClientByLogin(frame.getLogin());
            server.saveClientLogin(context.getId(), frame.getLogin());
            context.setLoginAndPassword(frame.getLogin(), "");
            BDDServerFrame bddFrame = BDDServerFrame.createBDDServerFrame(context.getId(), frame.getLogin());
            ByteBuffer tmp = ByteBuffer.allocate(BDD_BUFFER_SIZE);
            bddFrame.fillByteBuffer(tmp);
            server.sendRequestToBDD(tmp);
        } else {
            throw new UnsupportedOperationException("client does not send these kind of frames");
        }
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        Objects.requireNonNull(frame);
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
        Objects.requireNonNull(frame);
        server.broadcast(frame);
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        Objects.requireNonNull(frame);
        String dest = frame.getLogin();
        ServerToClientContext destClient = server.getClientByLogin(dest);
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        if(destClient == null) {
            PrivateConnectionResponseFrame response = PrivateConnectionResponseFrame
                    .createPrivateConnectionResponseFrame(PRIVATE_CONNECTION_KO, frame.getIdRequest());
            response.fillByteBuffer(tmp);
            context.queueMessage(tmp);
            return;
        }
        destClient.setPrivateClientConnection(context, frame.getIdRequest());
        PrivateConnectionFrame newFrame = PrivateConnectionFrame.createPrivateConnectionFrame(frame.getOpcode(),context.getLogin(),frame.getIdRequest(),frame.getAddress());
        newFrame.fillByteBuffer(tmp);
        destClient.queueMessage(tmp);
    }

    @Override
    public void visit(PrivateConnectionResponseFrame frame) {
        ByteBuffer buffer = ByteBuffer.allocate(1_024);
        frame.fillByteBuffer(buffer);
        context.queueMessage(buffer);
    }

    @Override
    public void visit(SimpleFrame frame) {
        Objects.requireNonNull(frame);
        int opcode = frame.getOpcode();
        ByteBuffer tmp = ByteBuffer.allocate(1_024);
        if(opcode == DISCONNECTION_REQUEST) { // disconnection request
            LOGGER.log(Level.INFO, "disconnection request");
            GlobalMessageFrame byeMessage = GlobalMessageFrame.createGlobalMessageFrame(GLOBAL_MESSAGE, "",
                    context.getLogin() + " has disconnected");
            server.broadcast(byeMessage);
            ByteBuffer bb = ByteBuffer.allocate(1024);
            SimpleFrame disconnectionFrame = SimpleFrame.createSimpleFrame(DISCONNECTION_OK,"You are disconnected");
            disconnectionFrame.fillByteBuffer(bb);
            context.queueMessage(bb);
            server.removeClient(context.getId());
        } else if(opcode == PRIVATE_CONNECTION_OK || opcode == PRIVATE_CONNECTION_KO) { // private connection response
            LOGGER.log(Level.INFO, "client has responded to private connection request");
            ServerToClientContext dest = context.getPrivateClientConnection();
            // reset private connection request parameters
            // to avoid false positives
            context.setPrivateClientConnection(null, -1);
            PrivateConnectionResponseFrame responseFrame = PrivateConnectionResponseFrame
                    .createPrivateConnectionResponseFrame(opcode, context.getRequestId());
            responseFrame.fillByteBuffer(tmp);
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
