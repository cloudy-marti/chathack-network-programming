package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
               byte       byte      String     int       String
            ----------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfData | Data |
            ----------------------------------------------------
 */
public class MessageFrame implements ChatHackFrame {

    private final int opCode;
    private final String login;
    private final String msg;
    private final ByteBuffer messageFrame;
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    //UTF8 for the message
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    public MessageFrame(int opCode, String login, String msg) {
        this.opCode = opCode;
        this.login = login;
        this.msg = msg;
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer loginConnection = ASCII.encode(login);
        int sizeOfLogin = loginConnection.remaining();
        ByteBuffer databb = UTF_8.encode(msg);
        int sizeOfData = databb.remaining();
        messageFrame = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + sizeOfLogin + Integer.BYTES + sizeOfData);
        messageFrame.put(opCodeByte);
        messageFrame.putInt(sizeOfLogin);
        messageFrame.put(loginConnection);
        messageFrame.putInt(sizeOfData);
        messageFrame.put(databb);
        messageFrame.flip();
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(messageFrame);
        messageFrame.flip();
        bbdst.flip();
    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
