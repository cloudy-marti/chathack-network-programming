package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private ConnectionFrame(int opcode, String login, ByteBuffer connectionFrame) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(connectionFrame);
        this.opcode = opcode;
        this.login = login;
        this.connectionFrame = connectionFrame;
    }

    public static ConnectionFrame createConnectionFrame(int opcode, String login) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = UTF_8.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer connectionFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfLogin);
        connectionFrame.put(opCodeByte);
        connectionFrame.putInt(sizeOfLogin);
        connectionFrame.put(loginConnection);
        connectionFrame.flip();

        return new ConnectionFrame(opcode, login, connectionFrame);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        if (checkBufferSize(bbdst)) {
            bbdst.put(connectionFrame);
            connectionFrame.flip();
            bbdst.flip();
            //bbdst.compact();
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
        return opcode;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
