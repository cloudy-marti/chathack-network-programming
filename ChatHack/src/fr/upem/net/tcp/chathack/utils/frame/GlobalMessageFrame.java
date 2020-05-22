package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/*
opCode : 20
               byte       int      String       int          String
            ----------------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfMessage | Message |
            ----------------------------------------------------------

ENCODING : ASCII for login/password & UTF 8 for Message
 */
public class GlobalMessageFrame implements ChatHackFrame {

    private final int opCode;
    private final String login;
    private final String msg;
    private final ByteBuffer globalMessageFrame;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private GlobalMessageFrame(int opCode, String login, String msg, ByteBuffer globalMessageFrame) {
        if (opCode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(msg);
        Objects.requireNonNull(globalMessageFrame);
        this.opCode = opCode;
        this.login = login;
        this.msg = msg;
        this.globalMessageFrame = globalMessageFrame;
    }

    /* Design Pattern Factory
     * Enlève le code complexe du constructeur
     * Peut cacher la classe
     *
     * Singleton : Garantie que dans toute la vm on n'a qu'une seule instance
     * */
    public static GlobalMessageFrame createGlobalMessageFrame(int opCode, String login, String msg) {
        if (opCode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(login);
        Objects.requireNonNull(msg);
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer loginConnection = UTF_8.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer databb = UTF_8.encode(msg);
        int sizeOfData = databb.remaining();

        ByteBuffer messageFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfLogin + Integer.BYTES + sizeOfData);
        messageFrame.put(opCodeByte);
        messageFrame.putInt(sizeOfLogin);
        messageFrame.put(loginConnection);
        messageFrame.putInt(sizeOfData);
        messageFrame.put(databb);
        messageFrame.flip();

        return new GlobalMessageFrame(opCode, login, msg, messageFrame);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        if (checkBufferSize(bbdst)) {
            bbdst.put(globalMessageFrame);
            globalMessageFrame.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }

    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return buffer.remaining() >= globalMessageFrame.remaining();
    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return opCode;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getLogin() {
        return this.login;
    }

    public String getMsg() {
        return this.msg;
    }
}
