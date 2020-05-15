package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.DataFrame;
import fr.upem.net.tcp.chathack.utils.frame.MessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;

public class ClientToServerFrameVisitor implements FrameVisitor {

    private final Context context;
    //private final ChatHackClient client;

    public ClientToServerFrameVisitor(Context context/*, ChatHackClient client*/) {
        this.context = context;
        //this.client = client;
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
