package fr.upem.net.tcp.chathack;

// junit 5.6.2
import fr.upem.net.tcp.chathack.utils.OpCodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class OpCodeTypeTest {

    @Test
    public void assertOpCodeTypeWorksWithConnectOpCodeType() {
        assertEquals(OpCodeType.CONNECT, OpCodeType.getOpCodeType(0)); // connect opcode
        assertNotEquals(OpCodeType.CONNECT, OpCodeType.getOpCodeType(10)); // ack opcode
    }

    @Test
    public void assertOpCodeTypeWorksWithAcquitOpCodeType() {
        assertEquals(OpCodeType.ACQUIT, OpCodeType.getOpCodeType(10)); // ack opcode
        assertNotEquals(OpCodeType.ACQUIT, OpCodeType.getOpCodeType(20)); // data transmission opcode
    }

    @Test
    public void assertOpCodeTypeWorksWithDataTransmissionOpCodeType() {
        assertEquals(OpCodeType.MESSAGE, OpCodeType.getOpCodeType(20)); // data transmission code
        assertNotEquals(OpCodeType.MESSAGE, OpCodeType.getOpCodeType(30)); // error code
    }

    @Test
    public void assertOpCodeTypeWorksWithErrorOpCodeType() {
        assertEquals(OpCodeType.ERROR, OpCodeType.getOpCodeType(30)); // error code
        assertNotEquals(OpCodeType.ERROR, OpCodeType.getOpCodeType(40)); // invalid code
    }
}
