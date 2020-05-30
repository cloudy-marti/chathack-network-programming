package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Private connection response frame
 *                         byte      long
 *                     -----------------------
 *                     | Opcode | idRequest |
 *                     ----------------------
 */
public class PrivateConnectionResponseFrame implements ChatHackFrame {
    private final int opcode;
    private final long idRequest;
    private final ByteBuffer connectionFrame;

    private PrivateConnectionResponseFrame(int opcode, long idRequest, ByteBuffer connectionFrame) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(connectionFrame);
        this.opcode = opcode;
        this.idRequest = idRequest;
        this.connectionFrame = connectionFrame;
    }

    public static PrivateConnectionResponseFrame createPrivateConnectionResponseFrame(int opcode, long idRequest) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer connectionFrame = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
        connectionFrame.put(opCodeByte);
        connectionFrame.putLong(idRequest);
        connectionFrame.flip();

        return new PrivateConnectionResponseFrame(opcode, idRequest, connectionFrame);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        Objects.requireNonNull(bbdst);
        if (checkBufferSize(bbdst)) {
            bbdst.put(connectionFrame);
            connectionFrame.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }

    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= connectionFrame.remaining());
    }

    @Override
    public void accept(FrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public long getIdRequest() {
        return idRequest;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
