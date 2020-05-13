package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;

public class DataFrame implements ChatHackFrame {

    public DataFrame() {

    }

    @Override
    public ByteBuffer asByteBuffer() {
        return null;
    }

    private static enum DataOpCode {
        GLOBAL_MESSAGE (20),
        PRIVATE_MESSAGE (21),
        PRIVATE_FILE (22);

        private final int opCode;

        DataOpCode(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return this.opCode;
        }
    }
}
