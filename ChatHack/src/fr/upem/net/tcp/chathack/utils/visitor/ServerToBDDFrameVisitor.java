package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

public class ServerToBDDFrameVisitor implements FrameVisitor {

    private final ServerToBDDContext context;
    //private final ChatHackServer server;

    public ServerToBDDFrameVisitor(ServerToBDDContext context) {
        this.context = context;
        //this.server = server;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        throw new UnsupportedOperationException("no connection frame between server and BDD server");
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("files cannot be exchanged between server and BDD server");
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        throw new UnsupportedOperationException("messages cannot be exchanged between server and BDD server");
    }

    @Override
    public void visit(SimpleFrame frame) {
        throw new UnsupportedOperationException("invalid frame");
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException("invalid frame");
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        throw new UnsupportedOperationException("invalid frame");
    }

    @Override
    public void visit(BDDServerFrame frame) {

    }

    @Override
    public void visit(BDDServerFrameWithPassword frame) {

    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {

    }
}
