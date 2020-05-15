package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.DataFrame;
import fr.upem.net.tcp.chathack.utils.frame.MessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;

public class ServerToClientFrameVisitor implements FrameVisitor {

    private final Context context;
    private final ChatHackServer server;

    public ServerToClientFrameVisitor(Context context, ChatHackServer server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectionFrame frame) {

    }

    @Override
    public void visit(DataFrame frame) {

    }

    @Override
    public void visit(MessageFrame frame) {

    }

    @Override
    public void visit(SimpleFrame frame) {

    }
}
