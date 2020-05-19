package fr.upem.net.tcp.chathack.utils.reader;

import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;

import java.nio.ByteBuffer;

public class TwoStringsReader implements Reader<GlobalMessageFrame> {
    private enum State {DONE, WAITING_LOGIN, WAITING_MSG, ERROR};

    private State state = State.WAITING_LOGIN;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String login;
    private String message;
    private int opcode;

    // opcodes 01, 20 and 21
    /* Format
     * +-------------------+------------+-----------------+-----------+
     * | Login size (BYTE) | Login ASCII| Text size (INT) | Text UTF8 |
     * +-------------------+------------+-----------------+-----------+
     */

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        //opcode = buffer.get() & 0xFF;

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
    public GlobalMessageFrame get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return GlobalMessageFrame.createGlobalMessageFrame(opcode, login, message);
    }

    @Override
    public void reset() {
        state = State.WAITING_LOGIN;
        internalBuffer.clear();
    }
}
