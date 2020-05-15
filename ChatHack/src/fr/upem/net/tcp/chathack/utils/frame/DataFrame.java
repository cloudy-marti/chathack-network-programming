package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
    private final ByteBuffer dataFrame;
    private final static Charset ASCII = StandardCharsets.US_ASCII;

    public DataFrame(int opCode, String fileName, byte[] fileData) {
        this.opCode = opCode;
        this.fileName = fileName;
        this.fileData = fileData;
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer fileNamebb = ASCII.encode(fileName);
        int sizeOfFileName = fileNamebb.remaining();
        int sizeOfData = fileData.length;
        dataFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfFileName + Integer.BYTES + sizeOfData);
        dataFrame.put(opCodeByte);
        dataFrame.putInt(sizeOfFileName);
        dataFrame.put(fileNamebb);
        dataFrame.putInt(sizeOfData);
        dataFrame.put(fileData);
        dataFrame.flip();

    }

    @Override
    public void asByteBuffer(ByteBuffer bbdst) {
        bbdst.put(dataFrame);
        dataFrame.flip();
        bbdst.flip();

    }

    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
