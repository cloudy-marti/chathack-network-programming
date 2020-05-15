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
    private final ByteBuffer fileData;
    private final static Charset ASCII = StandardCharsets.US_ASCII;

    private final ByteBuffer dataFrame;

    private DataFrame(int opCode, String fileName, ByteBuffer fileData, ByteBuffer dataFrame) {
        this.opCode = opCode;
        this.fileName = fileName;
        this.fileData = fileData;
        this.dataFrame = dataFrame;
    }

    public static DataFrame createDataFrame(int opCode, String fileName, ByteBuffer fileData) {
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer fileNamebb = ASCII.encode(fileName);
        int sizeOfFileName = fileNamebb.remaining();
        int sizeOfData = fileData.remaining();
        ByteBuffer dataFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfFileName + Integer.BYTES + sizeOfData);
        dataFrame.put(opCodeByte);
        dataFrame.putInt(sizeOfFileName);
        dataFrame.put(fileNamebb);
        dataFrame.putInt(sizeOfData);
        dataFrame.put(fileData);
        dataFrame.flip();

        return new DataFrame(opCode, fileName, fileData, dataFrame);
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

    @Override
    public int getOpcode() {
        return this.opCode;
    }
}
