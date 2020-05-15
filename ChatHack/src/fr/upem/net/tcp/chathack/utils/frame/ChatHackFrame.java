package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;

public interface ChatHackFrame {
    void fileByteBuffer(ByteBuffer bbdst);
    boolean checkBufferSize(ByteBuffer buffer);
    void accept(FrameVisitor visitor);
    int getOpcode();
}
