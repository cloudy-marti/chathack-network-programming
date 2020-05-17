package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
opCode : 01

               byte       int      String          int          String
            ------------------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfPassword | Password |
            ------------------------------------------------------------

ENCODING : ASCII
 */

public class LoginPasswordFrame implements ChatHackFrame {

    private final int opcode;
    private final String login;
    private final String password;
    private final ByteBuffer loginPasswordbb;
    private final static Charset ASCII = StandardCharsets.US_ASCII;

    private LoginPasswordFrame(int opcode, String login, String password, ByteBuffer loginPasswordbb) {
        this.opcode = opcode;
        this.login = login;
        this.password = password;
        this.loginPasswordbb = loginPasswordbb;
    }

    public LoginPasswordFrame createLoginPasswordFrame(int opcode, String login, String password) {
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = ASCII.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer passwordConnection = ASCII.encode(password);
        int sizeOfPassword = passwordConnection.remaining();
        ByteBuffer loginPasswordbb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfLogin + Integer.BYTES + sizeOfPassword);
        loginPasswordbb.put(opCodeByte);
        loginPasswordbb.putInt(sizeOfLogin);
        loginPasswordbb.put(loginConnection);
        loginPasswordbb.putInt(sizeOfPassword);
        loginPasswordbb.put(passwordConnection);
        loginPasswordbb.flip();

        return new LoginPasswordFrame(opcode, login, password, loginPasswordbb);

    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        if (checkBufferSize(bbdst)) {
            bbdst.put(loginPasswordbb);
            loginPasswordbb.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= loginPasswordbb.remaining());
    }

    @Override
    public void accept(FrameVisitor visitor) {

    }

    @Override
    public int getOpcode() {
        return 0;
    }
}
