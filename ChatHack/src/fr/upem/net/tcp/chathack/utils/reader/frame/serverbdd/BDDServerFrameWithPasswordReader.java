package fr.upem.net.tcp.chathack.utils.reader.frame.serverbdd;

import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerFrameWithPassword;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class BDDServerFrameWithPasswordReader implements Reader<BDDServerFrameWithPassword> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public BDDServerFrameWithPassword get() {
        return null;
    }

    @Override
    public BDDServerFrameWithPassword get(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {

    }
}
