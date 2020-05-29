package fr.upem.net.tcp.chathack.server;

import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.context.ServerToBDDContext;
import fr.upem.net.tcp.chathack.utils.context.ServerToClientContext;
import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// test commit
public class ChatHackServer {

    static private final int BUFFER_SIZE = 10_000;
    static private final Logger LOGGER = Logger.getLogger(ChatHackServer.class.getName());

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;

    // ChatHack context as a client to the MDP server
    private final SocketChannel socketChannel;
    private final InetSocketAddress bddServerAddress;
    private ServerToBDDContext uniqueContextBDD;


    private long id = 0;
    private final HashMap<Long, ServerToClientContext> clientsByID = new HashMap<>();
    private final HashMap<String, ServerToClientContext> clientsByLogin = new HashMap<>();

    public ChatHackServer(int port, InetSocketAddress bddServerAddress) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.selector = Selector.open();

        this.bddServerAddress = bddServerAddress;
        this.socketChannel = SocketChannel.open();
    }

    /**
     * Initialize the connections to the MDP server and open the server port.
     * Launch the server
     *
     * @throws IOException if an operation on the channels fails
     */
    public void launch() throws IOException {
        // connect to bdd server as a client
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        uniqueContextBDD = new ServerToBDDContext(key, this);
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
            if (key.isValid() && key.isConnectable()) {
                ((Context) key.attachment()).doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO,"Connection closed with client due to IOException");
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
        ServerToClientContext context = new ServerToClientContext(this, clientKey, id);
        clientKey.attach(context);

        clientsByID.put(id, context);
        id++;
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
        msg.fillByteBuffer(tmp);
//        clientsByID.forEach((id, client) -> client.queueMessage(tmp));
        Set<SelectionKey> selectionKeySet = selector.keys();
        for (SelectionKey key : selectionKeySet) {
            if(!(key.channel() instanceof ServerSocketChannel)) {
                if(key.attachment() instanceof ServerToClientContext) {
                    System.out.print("broadcasting to : ");
                    printSelectedKey(key);
                    ((Context)key.attachment()).queueMessage(tmp);
                }
            }
        }
    }


    /**
     * Send a request to the MDP server to know whether the login with/without password is valid
     * @param buffer frame to be sent to the MDP server
     */
    public void sendRequestToBDD(ByteBuffer buffer) {
        uniqueContextBDD.queueMessage(buffer);
    }

    public void registerClientMDP(String login, String password) {
        try(FileWriter fileWriter = new FileWriter("resources/save.txt", true)) {
            fileWriter.append(login).append("$").append(password);
        } catch (IOException ioE) {
            LOGGER.log(Level.SEVERE, "save.txt cannot be opened due to IOException");
        }
    }

    /**
     * Return the context of the client that is known by this id
     * @param id numeric value known only by the server
     * @return Client context with the corresponding id
     */
    public ServerToClientContext getClientById(long id) {
        return clientsByID.get(id);
    }

    /**
     * Return the context of the client that is known by this alias
     * @param login Alias chosen by the client itself and known by the server.
     * @return Client context with the corresponding login
     */
    public ServerToClientContext getClientByLogin(String login) {
        return clientsByLogin.get(login);
    }

    /**
     * Save the alias of the client next to its context
     * @param id numeric value corresponding to a client known only by the server
     * @param login Alias chosen by the client itself
     */
    public void saveClientLogin(long id, String login) {
        this.clientsByLogin.put(login, clientsByID.get(id));
    }

    /**
     * When a client is disconnected, remove it from connected clients of the server so it doesn't try to perform read
     * and write operations on it anymore.
     * @param id numeric value corresponding to the disconnected client
     */
    public void removeClient(long id) {
        ServerToClientContext client = clientsByID.get(id);
        clientsByLogin.remove(client.getLogin(), client);
        clientsByID.remove(id, client);
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 3) {
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