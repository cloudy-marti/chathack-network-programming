package fr.upem.net.tcp.chathack.utils.reader.frame.serverbdd;

import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class BDDServerFrameReader implements Reader<BDDServerFrameReader> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public BDDServerFrameReader get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BDDServerFrameReader get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {

    }
}
