package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
/*
               byte       byte      String     int       String
            ----------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfData | Data |
            ----------------------------------------------------



 */
public class ConnectionFrame implements ChatHackFrame {

    public ConnectionFrame() {

    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {

    }

    private static enum DataOpCode {
        CONNECTION_WITH_LOGIN((byte) 0),
        CONNECTION_WITH_LOGIN_AND_PASSWORD((byte) 1),
        PRIVATE_CONNECTION_REQUEST((byte) 2),
        DISCONNECTION_REQUEST((byte) 3);

        private final byte opCode;

        DataOpCode(byte opCode) {
            this.opCode = opCode;
        }

        public byte getOpCode() {
            return this.opCode;
        }
    }
}
