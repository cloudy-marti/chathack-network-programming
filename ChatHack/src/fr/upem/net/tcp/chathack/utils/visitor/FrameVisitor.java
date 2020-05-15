package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;

public interface FrameVisitor {
    void visit(ConnectionFrame frame);
    void visit(DataFrame frame);
    void visit(MessageFrame frame);
    void visit(SimpleFrame frame);
}
