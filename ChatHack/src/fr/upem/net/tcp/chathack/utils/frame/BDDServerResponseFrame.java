package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BDDServerResponseFrame implements ChatHackFrame {

    /*
             byte  Long
            ------------
            | 0/1 | id |
            ------------
 */

    private final long id;
    private final String login;
    private final String password;
    private final ByteBuffer bddBuffer;

    private BDDServerResponseFrame(long id, String login, String password, ByteBuffer bddBuffer) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.bddBuffer = bddBuffer;
    }

    public static BDDServerResponseFrame createBDDServerResponseFrame(long id, String login, String password) {
        ByteBuffer tmpLogin = StandardCharsets.UTF_8.encode(login);
        ByteBuffer tmpPassword = StandardCharsets.UTF_8.encode(password);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES +
                tmpLogin.remaining() + tmpPassword.remaining());
        buffer.putLong(id).put(tmpLogin).put(tmpPassword).flip();
        return new BDDServerResponseFrame(id, login, password, buffer);
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
