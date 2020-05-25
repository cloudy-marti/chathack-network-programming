package fr.upem.net.tcp.chathack.utils.context;

import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Context {

    int BUFFER_SIZE = 10_000;

    /**
     * Process the content of input buffer (bbin)
     * The convention is that bbin is in write-mode before the call
     * to process and after the call
     */
    void processIn();

    /**
     * Accept the frame using the visitor
     * @param frame Received frame
     */
    void treatFrame(ChatHackFrame frame);

    /**
     * Add a message to the message queue, tries to fill output buffer (bbOut) and updateInterestOps
     * @param msg frame in bytebuffer mode
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
     * The convention is that both buffers are in write-mode before the call
     * to updateInterestOps and after the call.
     * Also it is assumed that process has been be called just
     * before updateInterestOps.
     */
    void updateInterestOps();

    /**
     * Close the context's socketChannel
     */
    void silentlyClose();

    /**
     * Performs the read action on sc
     * The convention is that both buffers are in write-mode before the call
     * to doRead and after the call
     * @throws IOException if a read operation on a channel fails
     */
    void doRead() throws IOException;

    /**
     * Performs the write action on sc
     * The convention is that both buffers are in write-mode before the call
     * to doWrite and after the call
     * @throws IOException if a write operation on a channel fails
     */
    void doWrite() throws IOException;

    /**
     * Client to connect to a listening server
     * @throws IOException if a connect operation on a channel fails
     */
    void doConnect() throws IOException;
}
