package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;

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

    @Override
    public void visit(LoginPasswordFrame frame) {

    }

    @Override
    public void visit(PrivateConnectionFrame frame) {

    }

    @Override
    public void visit(BDDServerFrame frame) {

    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {

    }
}
