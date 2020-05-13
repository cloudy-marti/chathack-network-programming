package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
/*
               byte       byte      String
            --------------------------------
            | Opcode | SizeOfLogin | Login |
            --------------------------------
 */
public class ConnectionFrame implements ChatHackFrame {

    public static final int MASK = 0xff;

    public ConnectionFrame() {

    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {

    }
}
