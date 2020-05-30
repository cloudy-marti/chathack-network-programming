package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Multipurpose simple frame
 *                   byte     int   String
 *                 -----------------------
 *                 | Opcode | Size | Msg |
 *                 -----------------------
 */
public class SimpleFrame implements ChatHackFrame {
    private final int opcode;
    private final String message;
    private final ByteBuffer simpleFrame;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private SimpleFrame(int opcode, String message, ByteBuffer buffer) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(message);
        Objects.requireNonNull(buffer);
        this.opcode = opcode;
        this.message = message;
        this.simpleFrame = buffer;
    }

    public static SimpleFrame createSimpleFrame(int opCode, String message) {
        if (opCode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(message);
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer bbMsg = UTF_8.encode(message);
        int sizeMsg = bbMsg.remaining();
        ByteBuffer simpleFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeMsg);
        simpleFrame.put(opCodeByte);
        simpleFrame.putInt(sizeMsg);
        simpleFrame.put(bbMsg);
        simpleFrame.flip();

        return new SimpleFrame(opCode, message, simpleFrame);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        Objects.requireNonNull(bbdst);
        if (checkBufferSize(bbdst)) {
            bbdst.put(simpleFrame);
            simpleFrame.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= simpleFrame.remaining());
    }

    @Override
    public void accept(FrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return this.opcode;
    }

    @Override
    public String toString() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}
