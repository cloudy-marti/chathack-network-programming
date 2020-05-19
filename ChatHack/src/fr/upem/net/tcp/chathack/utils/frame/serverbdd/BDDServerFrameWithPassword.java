package fr.upem.net.tcp.chathack.utils.frame.serverbdd;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BDDServerFrameWithPassword implements ChatHackFrame {

    /*
            byte Long  String   String
            -----------------------------
            | 1 | id | Login | Password |
            -----------------------------
 */

    private final long id;
    private final String login;
    private final String password;
    private final ByteBuffer bddBuffer;

    private BDDServerFrameWithPassword(long id, String login, String password, ByteBuffer bddBuffer) {
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);
        Objects.requireNonNull(bddBuffer);
        this.id = id;
        this.login = login;
        this.password = password;
        this.bddBuffer = bddBuffer;
    }

    public static BDDServerFrameWithPassword createBDDServerFrameWithPassword(long id, String login, String password) {
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);
        ByteBuffer tmpLogin = StandardCharsets.UTF_8.encode(login);
        ByteBuffer tmpPassword = StandardCharsets.UTF_8.encode(password);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES +
                tmpLogin.remaining() + tmpPassword.remaining());
        buffer.putLong(id).put(tmpLogin).put(tmpPassword).flip();
        return new BDDServerFrameWithPassword(id, login, password, buffer);
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

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }

    @Override // unused
    public int getOpcode() {
        throw new UnsupportedOperationException("shouldn't be calling getOpcode on a frame without opcode");
    }
}
