package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerResponseFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.LongReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class BDDFrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE, WAITING_FOR_RESPONSE_CODE,WAITING_FOR_ID,ERROR};

    private State state = State.WAITING_FOR_RESPONSE_CODE;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode

    private long id;
    private byte response;

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.WAITING_FOR_RESPONSE_CODE) {
            bb.flip();
            try {
                if(!bb.hasRemaining()) {
                    return ProcessStatus.REFILL;
                }
                response = bb.get();
                state = State.WAITING_FOR_ID;
            } finally {
                bb.compact();
            }
        }

        LongReader longReader = new LongReader();
        if(state == State.WAITING_FOR_ID) {
            ProcessStatus status = longReader.process(bb);
            switch (status) {
                case DONE:
                    id = longReader.get();
                    state = State.DONE;
                    break;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }

        return ProcessStatus.DONE;
    }

    @Override
    public ChatHackFrame get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return BDDServerResponseFrame.createBDDServerResponseFrame(response, id);
    }

    @Override
    public ChatHackFrame get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        internalBuffer.clear();
        state = State.WAITING_FOR_RESPONSE_CODE;
    }
}
