package fr.upem.net.tcp.chathack.utils.frame;

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
        int loginSize = tmpLogin.remaining();
        ByteBuffer tmpPassword = StandardCharsets.UTF_8.encode(password);
        int passwordSize = tmpPassword.remaining();
        ByteBuffer buffer = ByteBuffer.allocate(1 + Long.BYTES + 2*Integer.BYTES + loginSize + passwordSize);
        byte opcode = 1;
        buffer.put(opcode).putLong(id).putInt(loginSize).put(tmpLogin).putInt(passwordSize).put(tmpPassword).flip();
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

    @Override // unused
    public void accept(FrameVisitor visitor) {
        throw new UnsupportedOperationException("write-only frame does not support visitor");
    }

    @Override // unused
    public int getOpcode() {
        throw new UnsupportedOperationException("shouldn't be calling getOpcode on a frame without opcode");
    }
}
