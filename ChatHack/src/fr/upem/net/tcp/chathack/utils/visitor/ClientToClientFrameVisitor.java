package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.FilesFrame;
import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;

public class ClientToClientFrameVisitor implements FrameVisitor {
    @Override
    public void visit(ConnectionFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(FilesFrame frame) {

    }

    @Override
    public void visit(GlobalMessageFrame frame) {

    }

    @Override
    public void visit(SimpleFrame frame) {

    }
}
