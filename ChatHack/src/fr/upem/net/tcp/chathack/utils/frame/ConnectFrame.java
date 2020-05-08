package fr.upem.net.tcp.chathack.utils.frame;

public class ConnectFrame {

    private static enum ConnectOpCode {
        CONNECTION_WITH_LOGIN (0),
        CONNECTION_WITH_LOGIN_AND_PASSWORD (1),
        PRIVATE_CONNECTION_REQUEST (2),
        DISCONNECTION_REQUEST (3);

        private final int opCode;

        ConnectOpCode(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return this.opCode;
        }
    }
}
