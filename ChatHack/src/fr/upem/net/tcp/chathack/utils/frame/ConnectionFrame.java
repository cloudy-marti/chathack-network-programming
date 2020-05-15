package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
               byte       byte      String
            --------------------------------
            | Opcode | SizeOfLogin | Login |
            --------------------------------
 */
public class ConnectionFrame implements ChatHackFrame {
    private final int opcode;
    private final String login;
    private final ByteBuffer connectionFrame;
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    public static final int MASK = 0xff;

    public ConnectionFrame(int opcode, String login) {
        this.opcode = opcode;
        this.login = login;
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = ASCII.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        connectionFrame = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + sizeOfLogin);
        connectionFrame.put(opCodeByte);
        connectionFrame.putInt(sizeOfLogin);
        connectionFrame.put(loginConnection);
        connectionFrame.flip();

    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(connectionFrame);
        connectionFrame.flip();
        bbdst.flip();
    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
