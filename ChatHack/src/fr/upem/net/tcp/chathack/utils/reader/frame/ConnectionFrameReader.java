package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class ConnectionFrameReader implements Reader<ConnectionFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public ConnectionFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionFrame get(int opcode) {
        return null;
    }

    @Override
    public void reset() {

    }
}
