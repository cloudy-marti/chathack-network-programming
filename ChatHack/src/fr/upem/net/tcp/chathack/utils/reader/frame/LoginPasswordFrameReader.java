package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.LoginPasswordFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.reader.utils.StringReader;

import java.nio.ByteBuffer;
import java.util.Objects;

public class LoginPasswordFrameReader implements Reader<LoginPasswordFrame> {

    private enum State {DONE, WAITING_LOGIN, WAITING_MSG, ERROR};

    private State state = State.WAITING_LOGIN;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String login;
    private String message;

    /*
     * +-------------------+-------+-----------------+------+
     * | Login size (BYTE) | Login | Text size (INT) | Text |
     * +-------------------+-------+-----------------+------+
     */

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        StringReader stringReader = new StringReader();
        if(state == State.WAITING_LOGIN) {
            ProcessStatus status = stringReader.process(buffer);
            switch (status) {
                case DONE:
                    login = stringReader.get();
                    state = State.WAITING_MSG;
                    break;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }

        stringReader.reset();
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
        return ProcessStatus.DONE;
    }

    @Override
    public LoginPasswordFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoginPasswordFrame get(int opcode) {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return LoginPasswordFrame.createLoginPasswordFrame(opcode, login, message);
    }

    @Override
    public void reset() {
        state = State.WAITING_LOGIN;
        internalBuffer.clear();
    }
}
