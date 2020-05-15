package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;

public interface ChatHackFrame {
    void asByteBuffer(ByteBuffer bbdst);
    void accept(FrameVisitor visitor);
}
