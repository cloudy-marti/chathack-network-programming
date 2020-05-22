package fr.upem.net.tcp.chathack.utils.frame;


import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/*
opCode : 02

               byte       int      String     byte            byte     int
            -----------------------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfAddress | Address | Port |
            -----------------------------------------------------------------
 */
public class PrivateConnectionFrame implements ChatHackFrame {
    private final int opcode;
    private final String login;
    private final InetSocketAddress address;
    private final ByteBuffer privateConnectionbb;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private PrivateConnectionFrame(int opcode, String login, InetSocketAddress address, ByteBuffer privateConnectionbb) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(privateConnectionbb);
        this.opcode = opcode;
        this.login = login;
        this.address = address;
        this.privateConnectionbb = privateConnectionbb;
    }

    public static PrivateConnectionFrame createPrivateConnectionFrame(int opcode, String login, InetSocketAddress address) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = UTF_8.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        byte[] byteAddress = address.getAddress().getAddress();
        byte sizeAddress = (byte) byteAddress.length;
        ByteBuffer privateConnectionbb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfLogin + Byte.BYTES + (Byte.BYTES + sizeAddress) + Integer.BYTES);
        privateConnectionbb.put(opCodeByte);
        privateConnectionbb.putInt(sizeOfLogin);
        privateConnectionbb.put(loginConnection);
        privateConnectionbb.put(sizeAddress);
        privateConnectionbb.put(byteAddress);
        privateConnectionbb.putInt(address.getPort());
        privateConnectionbb.flip();

        return new PrivateConnectionFrame(opcode, login, address, privateConnectionbb);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        if (checkBufferSize(bbdst)) {
            bbdst.put(privateConnectionbb);
            privateConnectionbb.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= privateConnectionbb.remaining());
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

    public InetSocketAddress getAddress() {
        return address;
    }
}
