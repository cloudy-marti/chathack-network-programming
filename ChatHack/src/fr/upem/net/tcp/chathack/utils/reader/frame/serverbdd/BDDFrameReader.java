package fr.upem.net.tcp.chathack.utils.reader.frame.serverbdd;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class BDDFrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE,WAITING_FOR_OPCODE,WAITING_FOR_FRAME,ERROR};

    private State state = State.WAITING_FOR_OPCODE;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode

    private ChatHackFrame frame;

    private Reader<? extends ChatHackFrame> currentReader;

    private final BDDServerFrameReader bddServerFrameReader = new BDDServerFrameReader();
    private final BDDServerFrameWithPasswordReader bddServerFrameWithPasswordReader = new BDDServerFrameWithPasswordReader();
    private final BDDServerResponseFrameReader bddServerResponseFrameReader = new BDDServerResponseFrameReader();

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        int opCode;
        switch (state) {
            case WAITING_FOR_OPCODE:
                bb.flip();
                try {
                    if(!bb.hasRemaining()) {
                        return ProcessStatus.REFILL;
                    }
                    opCode = bb.get() & 0xFF;
                }
                finally {
                    bb.compact();
                }
            case WAITING_FOR_FRAME:
                ProcessStatus status = currentReader.process(bb);
            case DONE:
            case ERROR:
        }
        return null;
    }

    @Override
    public ChatHackFrame get() {
        return null;
    }

    @Override
    public ChatHackFrame get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {

    }
}
