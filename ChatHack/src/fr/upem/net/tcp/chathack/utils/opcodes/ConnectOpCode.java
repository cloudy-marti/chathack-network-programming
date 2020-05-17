package fr.upem.net.tcp.chathack.utils.opcodes;

public enum ConnectOpCode {

    CONNECTION_WITH_LOGIN(0),
    CONNECTION_WITH_LOGIN_AND_PASSWORD(1),
    PRIVATE_CONNECTION_REQUEST(2),
    DISCONNECTION_REQUEST(3),
    PRESENTATION_LOGIN(4);

    private final int opCode;

    ConnectOpCode(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return this.opCode;
    }
}
