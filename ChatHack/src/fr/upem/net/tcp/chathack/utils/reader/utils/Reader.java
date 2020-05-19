package fr.upem.net.tcp.chathack.utils.reader.utils;

import java.nio.ByteBuffer;

public interface Reader<T> {
    enum ProcessStatus {DONE,REFILL,ERROR};
    ProcessStatus process(ByteBuffer bb);
    T get();
    T get(int opcode);
    void reset();
}