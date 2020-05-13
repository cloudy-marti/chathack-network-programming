package fr.upem.net.tcp.chathack.utils.frame;

import javax.xml.crypto.Data;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataFrame implements ChatHackFrame {

    /*
           byte        int          String       int       byte
        ----------------------------------------------------------
        | Opcode | SizeOfFileName | FileName | SizeOfData | Data |
        ----------------------------------------------------------
     */

    private final int opCode;
    private final String fileName;
    private final byte[] fileData;

    public DataFrame(int opCode, String fileName, byte[] fileData) {
        this.opCode = opCode;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer fileNameTmp = StandardCharsets.US_ASCII.encode(fileName);
        int sizeOfFileName = fileNameTmp.remaining();
        int sizeOfData = fileData.length;

        bbdst.put(opCodeByte).putInt(sizeOfFileName).put(fileNameTmp).putInt(sizeOfData).put(fileData).flip();
    }
}
