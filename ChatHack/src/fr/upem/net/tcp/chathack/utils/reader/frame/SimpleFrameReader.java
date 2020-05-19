package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.reader.utils.StringReader;

import java.nio.ByteBuffer;

/*
                  byte     int   String
                -----------------------
                | Opcode | Size | Msg |
                -----------------------

                ErrorFrame/AckFrame/ConnectionFrame/ResponseFrame
 */

public class SimpleFrameReader implements Reader<SimpleFrame> {

    private enum State {DONE, WAITING, ERROR};

    private State state = State.WAITING;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String message;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        StringReader stringReader = new StringReader();
        if(state == State.WAITING) {
            ProcessStatus status = stringReader.process(buffer);
            switch (status) {
                case DONE:
                    message = stringReader.get();
                    state = State.DONE;
                    return ProcessStatus.DONE;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }
        return ProcessStatus.DONE;
    }

    @Override
    public SimpleFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFrame get(int opcode) {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return SimpleFrame.createSimpleFrame(opcode, message);
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}
