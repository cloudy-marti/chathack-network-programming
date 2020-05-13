package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/*
                    byte        int        String
                --------------------------------------
                | Opcode | SizeOfErrorMsg | ErrorMsg |
                --------------------------------------
 */
public class ErrorFrame implements ChatHackFrame {
    private final ErrorOpCode opcode;

    private final String errorMessage;
    private final ByteBuffer errorFrame;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    public ErrorFrame(ErrorOpCode opcode, String errorMessage) {
        this.opcode = opcode;
        this.errorMessage = errorMessage;

        ByteBuffer errorMsg = UTF_8.encode(errorMessage);
        int sizeErrorMsg = errorMsg.remaining();
        errorFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeErrorMsg);

        errorFrame.put(opcode.getOpCode());
        errorFrame.putInt(sizeErrorMsg);
        errorFrame.put(errorMsg);
        errorFrame.flip();
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(errorFrame);
        errorFrame.flip();
    }

    private static enum ErrorOpCode {
        LOGIN_ERROR((byte) 30),
        LOGIN_WITH_PASSWORD_ERROR((byte) 31),
        LOST_FRAME((byte) 32),
        INVALID_ADDRESS((byte) 33),
        INVALID_PORT((byte) 34),
        DISCONNECTION_KO((byte) 35);

        private final byte opCode;

        ErrorOpCode(byte opCode) {
            this.opCode = opCode;
        }

        public byte getOpCode() {
            return this.opCode;
        }
    }
}
