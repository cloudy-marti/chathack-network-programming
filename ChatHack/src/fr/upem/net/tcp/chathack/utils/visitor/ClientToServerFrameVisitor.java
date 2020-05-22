package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.ClientToServerContext;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.serverbdd.BDDServerResponseFrame;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToServerFrameVisitor implements FrameVisitor {

    private static final Logger LOGGER = Logger.getLogger(ClientToServerFrameVisitor.class.getName());

    private final Context context;
    private final ChatHackClient client;
    private static final int BUFFER_SIZE = 10_000;

    public ClientToServerFrameVisitor(Context context, ChatHackClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public void visit(GlobalMessageFrame frame) {
        if (frame.getOpcode() == 20) {
            //System.out.println(frame);
            if (frame.getLogin().isEmpty()) { // message from the server
                System.out.println(frame.getMsg());
            } else {
                System.out.println(frame.getLogin() + ": " + frame.getMsg());
            }
        } else {
            throw new UnsupportedOperationException("this is not allowed on global chat.");
        }
    }

    @Override
    public void visit(SimpleFrame frame) {
        switch (frame.getOpcode()) {
            case 10:
            case 11:
                if (client.connected()) {
                    throw new IllegalStateException("The client is already connected.");
                }
                LOGGER.log(Level.INFO, "Response frame OK, server accepted the connection");
                client.setConnected();
                break;
            case 15:
            case 12:
                client.stop();
                break;
            case 13:
                // private connection accepted
                LOGGER.log(Level.INFO, "yay user accepted");
                // ouvrir la socket channel pour la connection
                break;
            case 14:
                // private connection not accepted
                LOGGER.log(Level.INFO, "nope user refused");
                break;
            default:
                throw new UnsupportedOperationException("this is not allowed");
        }
    }

    @Override
    public void visit(PrivateConnectionFrame frame) {
        try {
            client.getConnectionRequest().put(frame);
            System.out.println("Someone wants to send private messages to you. Do you accept ? Y/N");

            InetSocketAddress sc = frame.getAddress();
            // connect to requester
            //System.out.println(frame);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void visit(ConnectionFrame frame) {
        throw new UnsupportedOperationException("Connections are not allowed on global chat.");
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FileFrame frame) {
        throw new UnsupportedOperationException("Files are not allowed on global chat.");
    }

    @Override
    public void visit(BDDServerResponseFrame bddServerResponseFrame) {
        throw new UnsupportedOperationException("client does not interact with BDD server");
    }
}
