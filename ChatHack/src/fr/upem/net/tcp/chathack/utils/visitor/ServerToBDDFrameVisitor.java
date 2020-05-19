package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

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
        if(bddServerResponseFrame.isValid()) {

        } else {

        }
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

    @Override
    public void visit(BDDServerFrame frame) {
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }

    @Override
    public void visit(BDDServerFrameWithPassword frame) {
        throw new UnsupportedOperationException("server does not send bdd frames to client");
    }

}
