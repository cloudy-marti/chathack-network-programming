package fr.upem.net.tcp.chathack.utils.reader.utils;

import java.nio.ByteBuffer;

public interface Reader<T> {
    enum ProcessStatus {DONE,REFILL,ERROR};

    /**
     * Translate received ByteBuffer into an object
     * @param bb buffer read
     * @return ProcessStatus status of the translation : REFILL if it needs more reading to continue the translation,
     * DONE if the correct object is created and ERROR if the buffer does not have the expected format.
     */
    ProcessStatus process(ByteBuffer bb);

    /**
     * Get the object translated from the bytebuffer
     * @return T the object to be used on code
     */
    T get();

    /**
     * Get the Frame object translated from the bytebuffer that has the given opcode
     * @param opcode numerical value that is an ID to know with type of frame we received
     * @return T a ChatHackFrame object
     */
    T get(int opcode);

    /**
     * Return the reader into the initial state.
     */
    void reset();
}