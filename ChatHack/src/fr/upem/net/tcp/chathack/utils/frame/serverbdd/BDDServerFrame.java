package fr.upem.net.tcp.chathack.utils.frame.serverbdd;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BDDServerFrame implements ChatHackFrame {

    /*
            byte Long  String   String
            -----------------------------
            | 1 | id | Login | Password |
            -----------------------------
 */

    private final long id;
    private final String login;
    private final ByteBuffer bddBuffer;

    private BDDServerFrame(long id, String login, ByteBuffer bddBuffer) {
        Objects.requireNonNull(login);
        Objects.requireNonNull(bddBuffer);
        this.id = id;
        this.login = login;
        this.bddBuffer = bddBuffer;
    }

    public static BDDServerFrame createBDDServerFrame(long id, String login) {
        Objects.requireNonNull(login);
        ByteBuffer tmpLogin = StandardCharsets.UTF_8.encode(login);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES +
                tmpLogin.remaining());
        buffer.putLong(id).put(tmpLogin).flip();
        return new BDDServerFrame(id, login, buffer);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
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
        // visitor.visit(this);
    }

    @Override // unused
    public int getOpcode() {
        throw new UnsupportedOperationException("shouldn't be calling getOpcode on a frame without opcode");
    }
}
