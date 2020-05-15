package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.server.ChatHackServer;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.FilesFrame;
import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;
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
    public void visit(FilesFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(GlobalMessageFrame frame) {

    }

    @Override
    public void visit(SimpleFrame frame) {

    }
}
