package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.FileFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.ByteBufferReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.reader.utils.StringReader;

import java.nio.ByteBuffer;

public class FileFrameReader implements Reader<FileFrame> {

    private enum State {
        DONE, WAITING_FILE_NAME, WAITING_FILE, ERROR
    }

    private State state = State.WAITING_FILE_NAME;
    private static final int BUFFER_SIZE = 10_000;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private String fileName;
    private ByteBuffer fileData;

    /*
           int          String       int       byte
    -------------------------------------------------
    | SizeOfFileName | FileName | SizeOfFile | File |
    -------------------------------------------------
 */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_FILE_NAME) {
            StringReader stringReader = new StringReader();
            ProcessStatus status = stringReader.process(buffer);
            switch (status) {
                case DONE:
                    fileName = stringReader.get();
                    state = State.WAITING_FILE;
                    break;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }

        if(state == State.WAITING_FILE) {
            ByteBufferReader byteBufferReader = new ByteBufferReader();
            ProcessStatus status = byteBufferReader.process(buffer);
            switch (status) {
                case DONE:
                    fileData = byteBufferReader.get();
                    state = State.DONE;
                    break;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }

        return ProcessStatus.DONE;
    }

    @Override
    public FileFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileFrame get(int opcode) {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return FileFrame.createFileFrame(opcode, fileName, fileData);
    }

    @Override
    public void reset() {
        internalBuffer.clear();
        state = State.WAITING_FILE_NAME;
    }
}
