package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCodeType;

public class ServerToBDDFrameVisitor implements FrameVisitor {
    @Override
    public void visit(ConnectionFrame frame) {

    }

    @Override
    public void visit(FilesFrame frame) {

    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        int opcode = frame.getOpcode();
        OpCodeType type = OpCodeType.getOpCodeType(opcode);

        switch (type) {
            case CONNECT:
                break;
            case ACQUIT:
                break;
            case MESSAGE:
                break;
            case ERROR:
                break;
        }
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
