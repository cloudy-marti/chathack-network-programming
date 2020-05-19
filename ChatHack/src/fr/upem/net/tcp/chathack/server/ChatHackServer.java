package fr.upem.net.tcp.chathack.server;

import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame;
import fr.upem.net.tcp.chathack.utils.frame.ConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.LoginPasswordFrame;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatHackServer {

    static private final int BUFFER_SIZE = 10_000;
    static private final Logger LOGGER = Logger.getLogger(ChatHackServer.class.getName());

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;

    // ChatHack context as a client to the BDD server
    private final SocketChannel socketChannel;
    private final InetSocketAddress bddServerAddress;
    private ServerToBDDContext uniqueContextBDD;

    public ChatHackServer(int port, InetSocketAddress bddServerAddress) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.selector = Selector.open();

        this.bddServerAddress = bddServerAddress;
        this.socketChannel = SocketChannel.open();
    }

    public void launch() throws IOException {
        // connect to bdd server as a client
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        uniqueContextBDD = new ServerToBDDContext(key);
        key.attach(uniqueContextBDD);
        socketChannel.connect(bddServerAddress);

        // open server connection
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(!Thread.interrupted()) {
            printKeys(); // for debug
            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
            System.out.println("Select finished");
        }
    }

    private void treatKey(SelectionKey key) {
        printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch(IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((ServerToClientContext) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((ServerToClientContext) key.attachment()).doRead();
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO,"Connection closed with client due to IOException",e);
            silentlyClose(key);
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        if(client == null) {
            return;
        }
        client.configureBlocking(false);
        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
        clientKey.attach(new ServerToClientContext(this, clientKey));
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    /**
     * Add message to the global queue
     * @param msg frame to be sent to all clients (opcode 20)
     */
    public void broadcast(GlobalMessageFrame msg) {
        ByteBuffer tmp = ByteBuffer.allocate(BUFFER_SIZE);
        Set<SelectionKey> selectionKeySet = selector.keys();
        for (SelectionKey key : selectionKeySet) {
            if(!(key.channel() instanceof ServerSocketChannel)) {
                msg.fillByteBuffer(tmp);
                ((ServerToClientContext)key.attachment()).queueMessage(tmp);
            }
        }
    }

    public void sendRequestToBDD(ConnectionFrame frame) {
        //uniqueContextBDD.queueMessage(tmp);
    }

    public void sendRequestWithPasswordToBDD(LoginPasswordFrame frame) {
        //ByteBuffer tmp = ByteBuffer.allocate(1_024);

        // uniqueContextBDD.queueMessage();

    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length!=3){
            usage();
            return;
        }
        new ChatHackServer(Integer.parseInt(args[0]), new InetSocketAddress(args[1], Integer.parseInt(args[2])))
                .launch();
    }

    private static void usage(){
        System.out.println("Usage : ChatHackServer port bddServerAddress bddServerPort");
    }

    /***
     *  Theses methods are here to help understanding the behavior of the selector
     ***/

    private String interestOpsToString(SelectionKey key){
        if (!key.isValid()) {
            return "CANCELLED";
        }
        int interestOps = key.interestOps();
        ArrayList<String> list = new ArrayList<>();
        if ((interestOps&SelectionKey.OP_ACCEPT)!=0) list.add("OP_ACCEPT");
        if ((interestOps&SelectionKey.OP_READ)!=0) list.add("OP_READ");
        if ((interestOps&SelectionKey.OP_WRITE)!=0) list.add("OP_WRITE");
        return String.join("|",list);
    }

    public void printKeys() {
        Set<SelectionKey> selectionKeySet = selector.keys();
        if (selectionKeySet.isEmpty()) {
            System.out.println("The selector contains no key : this should not happen!");
            return;
        }
        System.out.println("The selector contains:");
        for (SelectionKey key : selectionKeySet){
            SelectableChannel channel = key.channel();
            if (channel instanceof ServerSocketChannel) {
                System.out.println("\tKey for ServerSocketChannel : "+ interestOpsToString(key));
            } else {
                SocketChannel sc = (SocketChannel) channel;
                System.out.println("\tKey for Client "+ remoteAddressToString(sc) +" : "+ interestOpsToString(key));
            }
        }
    }

    private String remoteAddressToString(SocketChannel sc) {
        try {
            return sc.getRemoteAddress().toString();
        } catch (IOException e){
            return "???";
        }
    }

    public void printSelectedKey(SelectionKey key) {
        SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
        } else {
            SocketChannel sc = (SocketChannel) channel;
            System.out.println("\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
        }
    }

    private String possibleActionsToString(SelectionKey key) {
        if (!key.isValid()) {
            return "CANCELLED";
        }
        ArrayList<String> list = new ArrayList<>();
        if (key.isAcceptable()) list.add("ACCEPT");
        if (key.isReadable()) list.add("READ");
        if (key.isWritable()) list.add("WRITE");
        return String.join(" and ",list);
    }
}
