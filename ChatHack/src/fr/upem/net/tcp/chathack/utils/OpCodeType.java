package fr.upem.net.tcp.chathack.utils;


public enum OpCodeType {
    CONNECT(10),                // 0-9
    ACQUIT(20),                 // 10-19
    MESSAGE(30),                // 20-29
    ERROR(40),                  // 30-39
    INVALID(Byte.MAX_VALUE);    // 40-127 (max byte value)

    private final int opCode;

    OpCodeType(int opCode) { // private default
        this.opCode = opCode;
    }

    /**
     * Which type corresponds to the given opCode
     * @param opCode of the frame
     * @return type of the opCode
     */
    public static OpCodeType getOpCodeType(int opCode) {
        for(OpCodeType opCodeType : values()) {
            if(opCode < opCodeType.opCode) {
                return opCodeType;
            }
        }
        return CONNECT;
    }
}