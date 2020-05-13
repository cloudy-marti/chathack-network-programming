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
    private final int opcode;
    private final String Message;
    private final ByteBuffer simpleFrame;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    public SimpleFrame(int opcode, String Message) {
        this.opcode = opcode;
        this.Message = Message;
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer errorMsg = UTF_8.encode(Message);
        int sizeErrorMsg = errorMsg.remaining();
        simpleFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeErrorMsg);
        simpleFrame.put(opCodeByte);
        simpleFrame.putInt(sizeErrorMsg);
        simpleFrame.put(errorMsg);
        simpleFrame.flip();
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(simpleFrame);
        simpleFrame.flip();
        bbdst.flip();
    }
}
