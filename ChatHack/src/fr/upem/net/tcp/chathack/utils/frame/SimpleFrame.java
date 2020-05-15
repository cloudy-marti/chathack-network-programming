package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

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

    private SimpleFrame(int opcode, String message, ByteBuffer buffer) {
        this.opcode = opcode;
        this.Message = message;
        this.simpleFrame = buffer;
    }

    public static SimpleFrame createSimpleFrame(int opCode, String message) {
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer errorMsg = UTF_8.encode(message);
        int sizeErrorMsg = errorMsg.remaining();
        ByteBuffer simpleFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeErrorMsg);
        simpleFrame.put(opCodeByte);
        simpleFrame.putInt(sizeErrorMsg);
        simpleFrame.put(errorMsg);
        simpleFrame.flip();

        return new SimpleFrame(opCode, message, simpleFrame);
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(simpleFrame);
        simpleFrame.flip();
        bbdst.flip();
    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return this.opcode;
    }
}
