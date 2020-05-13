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

    private static enum DataOpCode {
        CONNECTION_WITH_LOGIN(0),
        CONNECTION_WITH_LOGIN_AND_PASSWORD(1),
        PRIVATE_CONNECTION_REQUEST(2),
        DISCONNECTION_REQUEST(3);

        private final int opCode;

        DataOpCode(int opCode) {
            this.opCode = opCode & MASK;
        }

        public int getOpCode() {
            return opCode;
        }
    }
}
