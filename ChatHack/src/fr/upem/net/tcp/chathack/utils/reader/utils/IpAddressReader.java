package fr.upem.net.tcp.chathack.utils.reader.utils;

import java.nio.ByteBuffer;

public class IpAddressReader implements Reader<ByteBuffer> {

    private enum State {DONE,WAITING_SIZE,WAITING_BYTES,ERROR};

    private State state = State.WAITING_SIZE;
    private ByteBuffer internalBuffer; // write-mode
    private int size;
    private ByteBuffer value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if (state == State.WAITING_SIZE) {
            buffer.flip();
            try {
                if(!buffer.hasRemaining()) {
                    return ProcessStatus.REFILL;
                }
                size = buffer.get() & 0xFF;
                internalBuffer = ByteBuffer.allocate(size);
                state = State.WAITING_BYTES;
            } finally {
                buffer.compact();
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
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public ByteBuffer get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
    }
}
