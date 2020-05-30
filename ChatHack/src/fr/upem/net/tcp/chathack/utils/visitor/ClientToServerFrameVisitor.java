package fr.upem.net.tcp.chathack.utils.visitor;

import fr.upem.net.tcp.chathack.client.ChatHackClient;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;
import fr.upem.net.tcp.chathack.utils.frame.BDDServerResponseFrame;

import java.util.Objects;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

/**
 * Perform operations on received frames by the client from the server ChatHack
 */
public class ClientToServerFrameVisitor implements FrameVisitor {

    private final Context context;
    private final ChatHackClient client;

    public ClientToServerFrameVisitor(Context context, ChatHackClient client) {
        this.context = context;
        this.client = client;
    }

    /**
     * Message received by the server and to be displayed
     * @param frame that contains the message
     */
    @Override
    public void visit(GlobalMessageFrame frame) {
        Objects.requireNonNull(frame);
        if (frame.getOpcode() == GLOBAL_MESSAGE) {
            if (frame.getLogin().isEmpty()) { // message from the server
                System.out.println(frame.getMsg());
            } else { // global message from a client
                System.out.println(frame.getLogin() + ": " + frame.getMsg());
            }
        } else {
            throw new UnsupportedOperationException("this is not allowed on global chat.");
        }
    }

    /**
     * Connection and Disconnection acquittal frames
     * @param frame that contains the ack code
     */
    @Override
    public void visit(SimpleFrame frame) {
        Objects.requireNonNull(frame);
        switch (frame.getOpcode()) {
            case CONNECTION_WITH_LOGIN_OK:
            case CONNECTION_WITH_LOGIN_AND_PASSWORD_OK:
                if (client.connected()) {
                    throw new IllegalStateException("The client is already connected.");
                }
                System.out.println("Welcome to ChatHack !");
                client.setConnected();
                break;
            case DISCONNECTION_OK:
                System.out.println(frame.getMessage());
                System.exit(0);
                break;
            case CONNECTION_KO:
                client.stop();
                break;
            default:
                throw new UnsupportedOperationException("this is not allowed");
        }
    }

    /**
     * Private connection request from another client transmitted by the server
     * @param frame that contains the connection information
     */
    @Override
    public void visit(PrivateConnectionFrame frame) {
        Objects.requireNonNull(frame);
        try {
            client.getConnectionRequest().put(frame);
            System.out.println("Someone wants to send private messages to you. Do you accept ? ($accept/$refuse)");
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Private connection response from another client transmitted by the server
     * @param frame that contains the ack code
     */
    @Override
    public void visit(PrivateConnectionResponseFrame frame) {
        Objects.requireNonNull(frame);
        switch (frame.getOpcode()) {
            case PRIVATE_CONNECTION_OK:
                // private connection is accepted
                System.out.println("User accepted the private connection");
                client.getRequestWaiting().remove(frame.getIdRequest());
                break;
            case PRIVATE_CONNECTION_KO:
                // private connection not accepted
                System.out.println("User refused the private connexion");
                String login = client.getRequestWaiting().remove(frame.getIdRequest());
                client.getRefusedConnection().add(login);
            default:
        }
    }

    @Override
    public void visit(ConnectionFrame frame) {
        throw new UnsupportedOperationException("Connections are not allowed on global chat.");
    }

    @Override
    public void visit(LoginPasswordFrame frame) {
        throw new UnsupportedOperationException("This frame cannot be received by the client.");
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
