package fr.upem.net.tcp.chathack.utils.opcodes;

public enum ErrorOpCode {
    LOGIN_ERROR(30),
    LOGIN_WITH_PASSWORD_ERROR(31),
    LOST_FRAME(32),
    INVALID_ADDRESS(33),
    INVALID_PORT(34),
    DISCONNECTION_KO(35);

    private final int opCode;

    ErrorOpCode(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return this.opCode;
    }
}
