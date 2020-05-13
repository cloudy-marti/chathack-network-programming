package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;
/*
               byte       byte      String     int       String
            ----------------------------------------------------
            | Opcode | SizeOfLogin | Login | SizeOfData | Data |
            ----------------------------------------------------



 */
public class DataFrame implements ChatHackFrame {

    public DataFrame() {

    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {

    }

    private static enum DataOpCode {
        GLOBAL_MESSAGE((byte) 20),
        PRIVATE_MESSAGE((byte) 21),
        PRIVATE_FILE ((byte) 22);

        private final byte opCode;

        DataOpCode(byte opCode) {
            this.opCode = opCode;
        }

        public byte getOpCode() {
            return this.opCode;
        }
    }
}
