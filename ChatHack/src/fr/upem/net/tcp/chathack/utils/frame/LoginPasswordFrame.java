package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
    private final ByteBuffer loginPasswordBuffer;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private LoginPasswordFrame(int opcode, String login, String password, ByteBuffer loginPasswordBuffer) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);
        Objects.requireNonNull(loginPasswordBuffer);
        this.opcode = opcode;
        this.login = login;
        this.password = password;
        this.loginPasswordBuffer = loginPasswordBuffer;
    }

    public static LoginPasswordFrame createLoginPasswordFrame(int opcode, String login, String password) {
        if (opcode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);
        byte opCodeByte = Integer.valueOf(opcode).byteValue();
        ByteBuffer loginConnection = UTF_8.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer passwordConnection = UTF_8.encode(password);
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
            bbdst.put(loginPasswordBuffer);
            loginPasswordBuffer.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= loginPasswordBuffer.remaining());
    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }
}
