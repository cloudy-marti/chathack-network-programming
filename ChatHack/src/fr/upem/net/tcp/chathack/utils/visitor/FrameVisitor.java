package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;

public interface FrameVisitor {
    void visit(ConnectionFrame frame);
    void visit(FilesFrame frame);
    void visit(GlobalMessageFrame frame);
    void visit(SimpleFrame frame);
    void visit(LoginPasswordFrame frame);
    void visit(PrivateConnectionFrame frame);
    void visit(BDDServerFrame frame);
    void visit(BDDServerResponseFrame bddServerResponseFrame);
}
