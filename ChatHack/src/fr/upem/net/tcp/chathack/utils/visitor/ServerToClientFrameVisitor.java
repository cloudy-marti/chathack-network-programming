package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrame;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

public class ServerToClientFrameVisitor implements FrameVisitor {

    private final Context context;
    private final ChatHackServer server;

    public ServerToClientFrameVisitor(Context context, ChatHackServer server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        server.sendRequestToBDD(frame);
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        server.broadcast(frame);
    }

    @Override
    public void visit(SimpleFrame frame) {
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        server.sendRequestWithPasswordToBDD(frame);
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {

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
