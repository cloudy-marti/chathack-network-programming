package fr.upem.net.tcp.chathack.utils.reader;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;

import java.nio.ByteBuffer;

public class FrameReader implements Reader<ChatHackFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
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
        int opcode = bb.get() & MASK;
        OpCodeType opCode = OpCodeType.getOpCodeType(opcode);
        switch (opCode) {
            case CONNECT:
                // TODO
                break;
            case ACQUIT:
                // TODO
                break;
            case MESSAGE:
                // TODO
                break;
            case ERROR:
                // TODO
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

     */
}
