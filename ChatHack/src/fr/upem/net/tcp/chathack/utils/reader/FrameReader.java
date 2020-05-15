package fr.upem.net.tcp.chathack.utils.reader;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCodeType;

import java.nio.ByteBuffer;

public class FrameReader implements Reader<ChatHackFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        int opcode = bb.get() & 0xFF;
        OpCodeType opCode = OpCodeType.getOpCodeType(opcode);
        switch (opCode) {
            case CONNECT:
                // TODO
                //processConnect(bb);
                break;
            case ACQUIT:
                // TODO
                //processAcquit(bb);
                break;
            case MESSAGE:
                // TODO
                //processMessage(bb);
                break;
            case ERROR:
                // TODO
                //processError(bb);
                break;
            default:
                return ProcessStatus.ERROR;
        }
        return null;
    }


    @Override
    public ChatHackFrame get() {
        return null;
    }

    @Override
    public void reset() {

    }

    /*
    private static final int MASK = 0xff;

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        // TODO
        // get opCode from bb

    }

    @Override
    public ChatHackFrame get() {
        return null;
    }

    @Override
    public void reset() {

    }

     */
}
