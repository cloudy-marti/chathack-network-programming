package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCodeType;

public class ServerToBDDFrameVisitor implements FrameVisitor {
    @Override
    public void visit(ConnectionFrame frame) {

    }

    @Override
    public void visit(DataFrame frame) {

    }

    @Override
    public void visit(MessageFrame frame) {
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
}
