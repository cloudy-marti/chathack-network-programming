package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Server request to check a login to serverMDP
 *             byte Long  String   String
 *            -----------------------------
 *            | 1 | id | Login | Password |
 *            -----------------------------
 */
public class BDDServerFrame implements ChatHackFrame {

    private final ByteBuffer bddBuffer;

    private BDDServerFrame(String login, ByteBuffer bddBuffer) {
        Objects.requireNonNull(login);
        Objects.requireNonNull(bddBuffer);
        this.bddBuffer = bddBuffer;
    }

    public static BDDServerFrame createBDDServerFrame(long id, String login) {
        Objects.requireNonNull(login);
        ByteBuffer tmpLogin = StandardCharsets.UTF_8.encode(login);
        int loginSize = tmpLogin.remaining();
        ByteBuffer buffer = ByteBuffer.allocate(1 + Long.BYTES + Integer.BYTES + loginSize);
        byte opcode = 2;
        buffer.put(opcode).putLong(id).putInt(loginSize).put(tmpLogin).flip();
        return new BDDServerFrame(login, buffer);
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

    @Override // unused
    public void accept(FrameVisitor visitor) {
        throw new UnsupportedOperationException("write-only frame does not support visitor");
    }

    @Override // unused
    public int getOpcode() {
        throw new UnsupportedOperationException("shouldn't be calling getOpcode on a frame without opcode");
    }
}
