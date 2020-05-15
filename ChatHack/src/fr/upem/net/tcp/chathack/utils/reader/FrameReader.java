package fr.upem.net.tcp.chathack.utils.reader;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCodeType;

import java.nio.ByteBuffer;

public class FrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE,WAITING,ERROR};

    private State state = State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode
    private ChatHackFrame frame;

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
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return frame;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}
