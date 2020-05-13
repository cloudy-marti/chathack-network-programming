package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
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

    public MessageFrame(int opCode, String login, String msg) {
        this.opCode = opCode;
        this.login = login;
        this.msg = msg;
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        // login
        ByteBuffer tmpLogin = StandardCharsets.US_ASCII.encode(login);
        byte loginSize = Integer.valueOf(tmpLogin.remaining()).byteValue();
        // msg or password
        ByteBuffer tmpMsg = StandardCharsets.UTF_8.encode(msg);
        int msgSize = tmpMsg.remaining();

        bbdst.put(opCodeByte).put(loginSize).put(tmpLogin).putInt(msgSize).put(tmpMsg).flip();
    }
}
