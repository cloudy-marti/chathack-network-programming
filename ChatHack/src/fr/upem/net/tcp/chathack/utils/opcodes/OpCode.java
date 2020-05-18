package fr.upem.net.tcp.chathack.utils.opcodes;

public enum OpCode {

    CONNECTION_WITH_LOGIN(0),
    CONNECTION_WITH_LOGIN_AND_PASSWORD(1),
    PRIVATE_CONNECTION_REQUEST(2),
    DISCONNECTION_REQUEST(3),
    PRESENTATION_LOGIN(4),
    CONNECTION_WITH_LOGIN_OK(10),
    CONNECTION_WITH_LOGIN_AND_PASSWORD_OK(11),
    CONNECTION_WITH_REGISTER_OK(12),
    PRIVATE_CONNECTION_OK(13),
    PRIVATE_CONNECTION_KO(14),
    DISCONNECTION_OK(15),
    GLOBAL_MESSAGE(20),
    PRIVATE_MESSAGE(21),
    PRIVATE_FILE (22),
    LOGIN_ERROR(30),
    LOGIN_WITH_PASSWORD_ERROR(31),
    LOST_FRAME(32),
    INVALID_ADDRESS(33),
    INVALID_PORT(34),
    DISCONNECTION_KO(35);

    private final int opCode;

    OpCode(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return this.opCode;
    }
}
