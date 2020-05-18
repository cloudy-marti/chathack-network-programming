package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Context {

    int BUFFER_SIZE = 10_000;

    /**
     * Process the content of bbin
     * <p>
     * The convention is that bbin is in write-mode before the call
     * to process and after the call
     */
    void processIn();
    void treatFrame(ChatHackFrame frame);
    /**
     * Add a message to the message queue, tries to fill bbOut and updateInterestOps
     * @param msg
     */
    void queueMessage(ByteBuffer msg);
    /**
     * Try to fill bbout from the message queue
     */
    void processOut();
    /**
     * Update the interestOps of the key looking
     * only at values of the boolean closed and
     * of both ByteBuffers.
     * <p>
     * The convention is that both buffers are in write-mode before the call
     * to updateInterestOps and after the call.
     * Also it is assumed that process has been be called just
     * before updateInterestOps.
     */
    void updateInterestOps();
    void silentlyClose();
    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call
     * to doRead and after the call
     *
     * @throws IOException
     */
    void doRead() throws IOException;
    /**
     * Performs the write action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call
     * to doWrite and after the call
     *
     * @throws IOException
     */
    void doWrite() throws IOException;

    void doConnect() throws IOException;

}
