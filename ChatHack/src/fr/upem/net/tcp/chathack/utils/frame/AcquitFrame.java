package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;

public class AcquitFrame implements ChatHackFrame {

    public AcquitFrame() {

    }

    @Override
    public ByteBuffer asByteBuffer() {
        return null;
    }

    private static enum AcquitOpCode {
        CONNECTION_WITH_LOGIN_OK (10),
        CONNECTION_WITH_LOGIN_AND_PASSWORD_OK (11),
        CONNECTION_WITH_REGISTER_OK (12),
        PRIVATE_CONNECTION_OK (13),
        PRIVATE_CONNECTION_KO (14),
        DISCONNECTION_OK (15);

        private final int opCode;

        AcquitOpCode(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return this.opCode;
        }
    }
}
