package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FilesFrame implements ChatHackFrame {

    /*
opCode : 22
           byte        int          String       int       byte
        ----------------------------------------------------------
        | Opcode | SizeOfFileName | FileName | SizeOfFile | File |
        ----------------------------------------------------------

ENCODING : ASCII
     */

    private final int opCode;
    private final String fileName;
    private final ByteBuffer fileData;
    private final static Charset ASCII = StandardCharsets.US_ASCII;

    private final ByteBuffer fileFrame;

    private FilesFrame(int opCode, String fileName, ByteBuffer fileData, ByteBuffer fileFrame) {
        this.opCode = opCode;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileFrame = fileFrame;
    }

    public static FilesFrame createFilesFrame(int opCode, String fileName, ByteBuffer fileData) {
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

        return new FilesFrame(opCode, fileName, fileData, dataFrame);
    }

    @Override
    public void fileByteBuffer(ByteBuffer bbdst) {
        if(checkBufferSize(bbdst)){
            bbdst.put(fileFrame);
            fileFrame.flip();
            bbdst.flip();
        }
       throw new IllegalArgumentException();
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return (buffer.remaining() >= fileData.remaining());
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
