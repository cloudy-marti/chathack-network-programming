package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * ServerMDP's response frame when asked to check a login or pair of login/password
 *               byte  Long
 *             ------------
 *             | 0/1 | id |
 *             ------------
 */
public class BDDServerResponseFrame implements ChatHackFrame {

    private final byte isValid;
    private final long id;
    private final ByteBuffer bddBuffer;

    private BDDServerResponseFrame(byte isValid, long id, ByteBuffer bddBuffer) {
        Objects.requireNonNull(bddBuffer);
        this.isValid = isValid;
        this.id = id;
        this.bddBuffer = bddBuffer;
    }

    public static BDDServerResponseFrame createBDDServerResponseFrame(byte isValid, long id) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1);
        buffer.put(isValid).putLong(id).flip();
        return new BDDServerResponseFrame(isValid, id, buffer);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        Objects.requireNonNull(bbdst);
        if (checkBufferSize(bbdst)) {
            bbdst.put(bddBuffer);
            bddBuffer.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        return (buffer.remaining() >= bddBuffer.remaining());
    }

    @Override
    public void accept(FrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitor.visit(this);
    }

    @Override // unused
    public int getOpcode() {
        throw new UnsupportedOperationException("shouldn't be calling getOpcode on a frame without opcode");
    }

    public boolean isPresentOnBDD() {
        return (isValid & 0xFF) == 1;
    }

    public long getId() {
        return this.id;
    }
}
