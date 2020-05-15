package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientToClientContext implements Context {
    @Override
    public void processIn() {

    }

    @Override
    public void treatFrame(ChatHackFrame frame) {
        // frame.accept(frameVisitor);
    }

    @Override
    public void queueMessage(ByteBuffer msg) {

    }

    @Override
    public void processOut() {

    }

    @Override
    public void updateInterestOps() {

    }

    @Override
    public void silentlyClose() {

    }

    @Override
    public void doRead() {

    }

    @Override
    public void doWrite() {

    }

    @Override
    public void doConnect() throws IOException {

    }
}
