package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class FrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE,WAITING_FOR_OPCODE,WAITING_FOR_FRAME,ERROR};

    private State state = State.WAITING_FOR_OPCODE;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode

    private ChatHackFrame frame;
    private int opCode;

    private Reader<? extends ChatHackFrame> currentReader;

    private final ConnectionFrameReader connectionFrameReader = new ConnectionFrameReader();
    private final PrivateConnectionFrameReader privateConnectionFrameReader = new PrivateConnectionFrameReader();
    private final MessageFrameReader messageFrameReader = new MessageFrameReader();
    private final SimpleFrameReader simpleFrameReader = new SimpleFrameReader();
    private final FileFrameReader fileFrameReader = new FileFrameReader();

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        switch (state) {
            case WAITING_FOR_OPCODE:
                bb.flip();
                try {
                    if(!bb.hasRemaining()) {
                        return ProcessStatus.REFILL;
                    }
                    opCode = bb.get() & 0xFF;
                } finally {
                    bb.compact();
                }
                switch (opCode) {
                    case ChatHackFrame.CONNECTION_WITH_LOGIN:
                        currentReader = connectionFrameReader;
                        state = State.WAITING_FOR_FRAME;
                        break;
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD:
                    case ChatHackFrame.GLOBAL_MESSAGE:
                        currentReader = messageFrameReader;
                        state = State.WAITING_FOR_FRAME;
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_REQUEST:
                        currentReader = privateConnectionFrameReader;
                        state = State.WAITING_FOR_FRAME;
                        break;
                    case ChatHackFrame.PRIVATE_FILE:
                        currentReader = fileFrameReader;
                        state = State.WAITING_FOR_FRAME;
                        break;
                    case ChatHackFrame.PRIVATE_MESSAGE:
                    case ChatHackFrame.DISCONNECTION_REQUEST:
                    case ChatHackFrame.PRESENTATION_LOGIN: // ??
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_OK:
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD_OK:
                    case ChatHackFrame.CONNECTION_KO:
                    case ChatHackFrame.PRIVATE_CONNECTION_OK:
                    case ChatHackFrame.PRIVATE_CONNECTION_KO:
                    case ChatHackFrame.DISCONNECTION_OK:
                    case ChatHackFrame.DISCONNECTION_KO:
                    case ChatHackFrame.LOGIN_ERROR:
                    case ChatHackFrame.LOGIN_WITH_PASSWORD_ERROR:
                    case ChatHackFrame.INVALID_ADDRESS:
                    case ChatHackFrame.INVALID_PORT:
                        currentReader = simpleFrameReader;
                        state = State.WAITING_FOR_FRAME;
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
        state = State.WAITING_FOR_OPCODE;
        internalBuffer.clear();
    }
}
