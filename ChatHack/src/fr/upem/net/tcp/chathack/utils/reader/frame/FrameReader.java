package fr.upem.net.tcp.chathack.utils.reader;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCode;
import fr.upem.net.tcp.chathack.utils.opcodes.OpCodeType;

import java.nio.ByteBuffer;

public class FrameReader implements Reader<ChatHackFrame> {

    private enum State {DONE,WAITING_FOR_OPCODE,WAITING_FOR_FRAME,ERROR};

    private State state = State.WAITING_FOR_OPCODE;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode

    private ChatHackFrame frame;

    private Reader<? extends ChatHackFrame> currentReader;

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        switch (state) {
            case WAITING_FOR_OPCODE:
                bb.flip();
                int opCode;
                try {
                    if(!bb.hasRemaining()) {
                        return ProcessStatus.REFILL;
                    }
                    opCode = bb.get() & 0xFF;
                }
                finally {
                    bb.compact();
                }
                switch (opCode) {
                    case ChatHackFrame.CONNECTION_WITH_LOGIN:
                        break;
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD:
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_REQUEST:
                        break;
                    case ChatHackFrame.DISCONNECTION_REQUEST:
                        break;
                    case ChatHackFrame.PRESENTATION_LOGIN:
                        break;
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_OK:
                        break;
                    case ChatHackFrame.CONNECTION_WITH_LOGIN_AND_PASSWORD_OK:
                        break;
                    case ChatHackFrame.CONNECTION_WITH_REGISTER_OK:
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_OK:
                        break;
                    case ChatHackFrame.PRIVATE_CONNECTION_KO:
                        break;
                    case ChatHackFrame.DISCONNECTION_OK:
                        break;
                    case ChatHackFrame.DISCONNECTION_KO:
                        break;
                    case ChatHackFrame.GLOBAL_MESSAGE:
                        break;
                    case ChatHackFrame.PRIVATE_MESSAGE:
                        break;
                    case ChatHackFrame.PRIVATE_FILE:
                        break;
                    case ChatHackFrame.LOGIN_ERROR:
                        break;
                    case ChatHackFrame.LOGIN_WITH_PASSWORD_ERROR:
                        break;
                    case ChatHackFrame.INVALID_ADDRESS:
                        break;
                    case ChatHackFrame.INVALID_PORT:
                        break;
                    default:
                        state = State.ERROR;
                        return ProcessStatus.ERROR;
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
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return frame;
    }

    @Override
    public void reset() {
        state = State.WAITING_FOR_OPCODE;
        internalBuffer.clear();
    }
}
