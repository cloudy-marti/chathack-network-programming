package fr.upem.net.tcp.chathack.utils.reader.frame.bdd;

import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class BDDServerResponseFrameReader implements Reader<BDDServerResponseFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public BDDServerResponseFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BDDServerResponseFrame get(int opcode) {
        return null;
    }

    @Override
    public void reset() {

    }
}
