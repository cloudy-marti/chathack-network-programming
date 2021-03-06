package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;
import java.util.Objects;

public class FrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE,WAITING_FOR_OPCODE,WAITING_FOR_FRAME,ERROR};

    private State state = State.WAITING_FOR_OPCODE;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode

    private ChatHackFrame frame;
    private int opCode;

    private Reader<? extends ChatHackFrame> currentReader;

    private final ConnectionFrameReader connectionFrameReader = new ConnectionFrameReader();
    private final LoginPasswordFrameReader loginPasswordFrameReader = new LoginPasswordFrameReader();
    private final PrivateConnectionFrameReader privateConnectionFrameReader = new PrivateConnectionFrameReader();
    private final MessageFrameReader messageFrameReader = new MessageFrameReader();
    private final SimpleFrameReader simpleFrameReader = new SimpleFrameReader();
    private final FileFrameReader fileFrameReader = new FileFrameReader();
    private final PrivateConnectionResponseReader privateConnectionResponseReader = new PrivateConnectionResponseReader();

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        Objects.requireNonNull(bb);
        switch (state) {
            case WAITING_FOR_OPCODE:
                bb.flip();
                try {
                    if(!bb.hasRemaining()) {
                        return ProcessStatus.REFILL;
                    }
                    opCode = bb.get();
                    state = State.WAITING_FOR_FRAME;
                } finally {
                    bb.compact();
                }

                switch (opCode) {
                    case ChatHackFrame.PRESENTATION_LOGIN:
                    case ChatHackFrame.CONNECTION_WITH_LOGIN:
                        currentReader = connectionFrameReader;
                        break;
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD:
                        currentReader = loginPasswordFrameReader;
                        break;
                    case ChatHackFrame.GLOBAL_MESSAGE:
                        currentReader = messageFrameReader;
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_OK:
                    case ChatHackFrame.PRIVATE_CONNECTION_KO:
                        currentReader =privateConnectionResponseReader;
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_REQUEST:
                        currentReader = privateConnectionFrameReader;
                        break;
                    case ChatHackFrame.PRIVATE_FILE:
                        currentReader = fileFrameReader;
                        break;
                    case ChatHackFrame.PRIVATE_MESSAGE:
                    case ChatHackFrame.DISCONNECTION_REQUEST:
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_OK:
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD_OK:
                    case ChatHackFrame.CONNECTION_KO:
                    case ChatHackFrame.DISCONNECTION_OK:
                    case ChatHackFrame.DISCONNECTION_KO:
                    case ChatHackFrame.LOGIN_ERROR:
                    case ChatHackFrame.LOGIN_WITH_PASSWORD_ERROR:
                    case ChatHackFrame.INVALID_ADDRESS:
                    case ChatHackFrame.INVALID_PORT:
                        currentReader = simpleFrameReader;
                        break;
                    default:
                        state = State.ERROR;
                        return ProcessStatus.ERROR;
                }
            case WAITING_FOR_FRAME:
                ProcessStatus status = currentReader.process(bb);
                switch (status) {
                    case REFILL:
                        return ProcessStatus.REFILL;
                    case ERROR:
                        return ProcessStatus.ERROR;
                    case DONE:
                        state = State.DONE;
                        break;
                }
            case DONE:
                frame = currentReader.get(opCode);
                state = State.DONE;
                break;
            case ERROR:
                return ProcessStatus.ERROR;
        }
        return ProcessStatus.DONE;
    }


    @Override
    public ChatHackFrame get() {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return frame;
    }

    @Override
    public ChatHackFrame get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        connectionFrameReader.reset();
        privateConnectionFrameReader.reset();
        messageFrameReader.reset();
        simpleFrameReader.reset();
        fileFrameReader.reset();

        state = State.WAITING_FOR_OPCODE;
        internalBuffer.clear();
    }
}
