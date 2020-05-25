package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerResponseFrame;

/**
 * FrameVisitor Classes will perform the needed operations on each type of frame.
 * The method called will depend on with Class of ChatHackFrame is given as a parameter.
 */
public interface FrameVisitor {
    void visit(ConnectionFrame frame);
    void visit(FileFrame frame);
    void visit(GlobalMessageFrame frame);
    void visit(SimpleFrame frame);
    void visit(LoginPasswordFrame frame);
    void visit(PrivateConnectionFrame frame);
    void visit(BDDServerResponseFrame bddServerResponseFrame);
    void visit(PrivateConnectionResponseFrame frame);
}
