package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.PrivateConnectionResponseFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.LongReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;
import java.util.Objects;

public class PrivateConnectionResponseReader implements Reader<PrivateConnectionResponseFrame> {
    private enum State {
        DONE, WAITING, ERROR
    }

    private State state = State.WAITING;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private final LongReader longReader = new LongReader();
    private long idRequest;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if (state == State.WAITING) {
            ProcessStatus status = longReader.process(buffer);
            switch (status) {
                case DONE:
                    idRequest = longReader.get();
                    state = State.DONE;
                    return ProcessStatus.DONE;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    state = State.ERROR;
                    return ProcessStatus.ERROR;
            }
        }
        return ProcessStatus.DONE;
    }

    @Override
    public PrivateConnectionResponseFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrivateConnectionResponseFrame get(int opcode) {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return PrivateConnectionResponseFrame.createPrivateConnectionResponseFrame(opcode, idRequest);
    }

    @Override
    public void reset() {
        state = State.WAITING;
        longReader.reset();
        internalBuffer.clear();
    }
}
