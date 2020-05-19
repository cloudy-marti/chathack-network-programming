package fr.upem.net.tcp.chathack.utils.reader.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<String> {

    private enum State {DONE, WAITING_SIZE, WAITING_BYTES, ERROR};

    private State state = State.WAITING_SIZE;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private int size;
    private String value;

    /* Format :
     * +------------+------------+
     * | Size (INT) |  Text UTF8 |
     * +------------+------------+
     */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SIZE) {
            //internalBuffer.limit(Integer.BYTES);
            IntReader sizeReader = new IntReader();
            ProcessStatus status = sizeReader.process(buffer);
            switch (status) {
                case DONE:
                    size = sizeReader.get();
                    if(size <= BUFFER_SIZE && size >= 0) {
                        state = State.WAITING_BYTES;
                        break;
                    } else {
                        state = State.ERROR;
                        return ProcessStatus.ERROR;
                    }
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    //return ProcessStatus.ERROR;
                    throw new AssertionError();
            }
        }

        buffer.flip();
        internalBuffer.limit(size);
        try {
            if(buffer.remaining() <= internalBuffer.remaining()) {
                internalBuffer.put(buffer);
            } else {
                int oldLimit = buffer.limit();
                buffer.limit(internalBuffer.remaining());
                internalBuffer.put(buffer);
                buffer.limit(oldLimit);
            }
        } finally {
            buffer.compact();
        }

        if(internalBuffer.position() < size) {
            return ProcessStatus.REFILL;
        }
        state = State.DONE;
        internalBuffer.flip();
        internalBuffer.limit(size);
        value = StandardCharsets.UTF_8.decode(internalBuffer).toString();

        return ProcessStatus.DONE;
    }

    @Override
    public String get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public String get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
    }
}
