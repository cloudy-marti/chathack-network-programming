package fr.upem.net.tcp.chathack.utils.frame;


import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
opCode : 02

               byte       int      String     byte            byte     int
            -----------------------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfAddress | Address | Port |
            -----------------------------------------------------------------

ENCODING : ASCII
 */
public class PrivateConnectionFrame implements ChatHackFrame {
    private final int opcode;
    private final String login;
    private final InetSocketAddress address;
    private final ByteBuffer privateConnectionbb;
    private final static Charset ASCII = StandardCharsets.US_ASCII;

    private PrivateConnectionFrame(int opcode, String login, InetSocketAddress address, ByteBuffer privateConnectionbb) {
        this.opcode = opcode;
        this.login = login;
        this.address = address;
        this.privateConnectionbb = privateConnectionbb;
    }

    public static PrivateConnectionFrame createPrivateConnectionFrame(int opcode, String login, InetSocketAddress address) {
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = ASCII.encode(login);
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

    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
