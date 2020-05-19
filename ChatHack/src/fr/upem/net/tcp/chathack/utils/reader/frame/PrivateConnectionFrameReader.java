package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.PrivateConnectionFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class PrivateConnectionFrameReader implements Reader<PrivateConnectionFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public PrivateConnectionFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrivateConnectionFrame get(int opcode) {
        return null;
    }

    @Override
    public void reset() {

    }
}
