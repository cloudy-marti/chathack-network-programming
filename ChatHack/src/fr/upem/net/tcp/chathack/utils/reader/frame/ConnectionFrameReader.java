package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.reader.utils.StringReader;

import java.nio.ByteBuffer;

public class ConnectionFrameReader implements Reader<ConnectionFrame> {

    private enum State {
        DONE, WAITING, ERROR
    }

    private State state = State.WAITING;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String login;

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
                    login = stringReader.get();
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
    public ConnectionFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionFrame get(int opcode) {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return ConnectionFrame.createConnectionFrame(opcode, login);
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}
