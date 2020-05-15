package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
opCode : 00 03

               byte       int      String
            --------------------------------
            | Opcode | SizeOfLogin | Login |
            --------------------------------

ENCODING : ASCII
 */
public class ConnectionFrame implements ChatHackFrame {
    private final int opcode;
    private final String login;
    private final ByteBuffer connectionFrame;
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    // public static final int MASK = 0xff;

    private ConnectionFrame(int opcode, String login, ByteBuffer connectionFrame) {
        this.opcode = opcode;
        this.login = login;
        this.connectionFrame = connectionFrame;
    }

    public ConnectionFrame createConnectionFrame(int opcode, String login) {
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = ASCII.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer connectionFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfLogin);
        connectionFrame.put(opCodeByte);
        connectionFrame.putInt(sizeOfLogin);
        connectionFrame.put(loginConnection);
        connectionFrame.flip();

        return new ConnectionFrame(opcode, login, connectionFrame);
    }

    @Override
    public void fileByteBuffer(ByteBuffer bbdst) {
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
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return this.opcode;
    }
}
