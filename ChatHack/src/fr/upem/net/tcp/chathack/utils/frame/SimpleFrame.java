package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/*
                  byte     int   String
                -----------------------
                | Opcode | Size | Msg |
                -----------------------

                ErrorFrame/AckFrame/ConnectionFrame/ResponseFrame
 */
public class SimpleFrame implements ChatHackFrame {
    private final ErrorOpCode opcode;

    private final String Message;
    private final ByteBuffer simpleFrame;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    public SimpleFrame(ErrorOpCode opcode, String Message) {
        this.opcode = opcode;
        this.Message = Message;

        ByteBuffer errorMsg = UTF_8.encode(Message);
        int sizeErrorMsg = errorMsg.remaining();
        simpleFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeErrorMsg);

        simpleFrame.put(opcode.getOpCode());
        simpleFrame.putInt(sizeErrorMsg);
        simpleFrame.put(errorMsg);
        simpleFrame.flip();
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(simpleFrame);
        simpleFrame.flip();
    }

    private static enum ErrorOpCode {


        CONNECTION_WITH_LOGIN_OK((byte) 10),
        CONNECTION_WITH_LOGIN_AND_PASSWORD_OK((byte) 11),
        CONNECTION_WITH_REGISTER_OK((byte) 12),
        PRIVATE_CONNECTION_OK((byte) 13),
        PRIVATE_CONNECTION_KO((byte) 14),
        DISCONNECTION_OK((byte) 15),
        GLOBAL_MESSAGE((byte) 20),
        PRIVATE_MESSAGE((byte) 21),
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
