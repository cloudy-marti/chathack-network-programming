package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.ClientToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerResponseFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

public class ClientToClientFrameVisitor implements FrameVisitor {
    private final ClientToClientContext context;
    private final ChatHackClient client;
    private static final int BUFFER_SIZE = 10_000;

    public ClientToClientFrameVisitor(ClientToClientContext context, ChatHackClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public void visit(ConnectionFrame frame) {
        if (frame.getOpcode() == PRESENTATION_LOGIN) {
            context.setLogin(frame.getLogin());
            client.getContextPrivateConnection().put(frame.getLogin(), context);
            var queue = client.getWaitingMessage().get(frame.getLogin());

            while (!queue.isEmpty()) {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                var privateMessage = SimpleFrame.createSimpleFrame(PRIVATE_MESSAGE, queue.poll());
                privateMessage.fillByteBuffer(buffer);
                context.queueMessage(buffer);
                System.out.println("Send to : " + context.getLogin() + " -> " + privateMessage.getMessage() );
            }
        } else {
            throw new UnsupportedOperationException("The client can't receive this");
        }
    }

    @Override
    public void visit(FileFrame frame) {
        File outputFile = new File("resources/" + client.getLogin() + "/" + frame.getFileName());
        try {
            System.out.println("Sending file "+ frame.getFileName() + " ...");
            FileChannel channel = new FileOutputStream(outputFile).getChannel();
            channel.write(frame.getFileData());
        } catch (IOException ioE) {
            System.out.println("Couldn't create file");
        }
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(SimpleFrame frame) {
        switch (frame.getOpcode()) {
            case PRIVATE_MESSAGE:
                System.out.println("Message receive from : " + context.getLogin() + " -> " + frame);
                break;
            default:
                throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
        }
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        throw new UnsupportedOperationException("Connection frames between clients are not allowed.");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("BDD frames between clients are not allowed");
    }

    @Override
    public void visit(PrivateConnectionResponseFrame frame) {

    }
}
