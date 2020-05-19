package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.FileFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;

import java.nio.ByteBuffer;

public class FileFrameReader implements Reader<FileFrame> {
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public FileFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileFrame get(int opcode) {
        return null;
    }

    @Override
    public void reset() {

    }
}
