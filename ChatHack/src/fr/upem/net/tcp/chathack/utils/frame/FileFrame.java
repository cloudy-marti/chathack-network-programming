package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FileFrame implements ChatHackFrame {

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
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private final ByteBuffer fileFrame;

    private FileFrame(int opCode, String fileName, ByteBuffer fileData, ByteBuffer fileFrame) {
        if (opCode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(fileData);
        Objects.requireNonNull(fileFrame);
        this.opCode = opCode;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileFrame = fileFrame;
    }

    public static FileFrame createFileFrame(int opCode, String fileName, ByteBuffer fileData) {
        if (opCode < 0) {
            throw new IllegalArgumentException("OpCode can't be a negative value");
        }
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(fileData);
        byte opCodeByte = Integer.valueOf(opCode).byteValue();
        ByteBuffer fileNamebb = UTF_8.encode(fileName);
        int sizeOfFileName = fileNamebb.remaining();
        int sizeOfData = fileData.remaining();
        ByteBuffer dataFrame = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + sizeOfFileName + Integer.BYTES + sizeOfData);
        dataFrame.put(opCodeByte);
        dataFrame.putInt(sizeOfFileName);
        dataFrame.put(fileNamebb);
        dataFrame.putInt(sizeOfData);
        dataFrame.put(fileData);
        dataFrame.flip();

        return new FileFrame(opCode, fileName, fileData, dataFrame);
    }

    @Override
    public void fillByteBuffer(ByteBuffer bbdst) {
        Objects.requireNonNull(bbdst);
        if (checkBufferSize(bbdst)) {
            bbdst.put(fileFrame);
            fileFrame.flip();
            bbdst.flip();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean checkBufferSize(ByteBuffer buffer) {
        //buffer in write mode
        return buffer.remaining() >= fileData.remaining();
    }

    @Override
    public void accept(FrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitor.visit(this);
    }

    @Override
    public int getOpcode() {
        return opCode;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getFileName() {
        return this.fileName;
    }

    public ByteBuffer getFileData() {
        return this.fileData;
    }
}
