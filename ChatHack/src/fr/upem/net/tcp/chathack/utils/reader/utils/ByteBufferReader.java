package fr.upem.net.tcp.chathack.utils.reader.utils;

import java.nio.ByteBuffer;

public class ByteBufferReader implements Reader<ByteBuffer> {

    private enum State {DONE,WAITING_SIZE,WAITING_BYTES,ERROR};

    private State state = State.WAITING_SIZE;
    private final static int BUFFER_SIZE = 10_000;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private int size;
    private ByteBuffer value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SIZE) {
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

        value = internalBuffer;

        return ProcessStatus.DONE;
    }

    @Override
    public ByteBuffer get() {
        return null;
    }

    @Override
    public ByteBuffer get(int opcode) {
        return null;
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
    }
}
