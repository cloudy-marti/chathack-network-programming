package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.DataFrame;
import fr.upem.net.tcp.chathack.utils.frame.MessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;

public class ClientToClientFrameVisitor implements FrameVisitor {
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
