package fr.upem.net.tcp.chathack.utils.frame;

import java.nio.ByteBuffer;

public interface ChatHackFrame {

    void asByteBuffer(ByteBuffer bbdst);

}
