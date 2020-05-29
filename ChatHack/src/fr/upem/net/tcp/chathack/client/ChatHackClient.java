package fr.upem.net.tcp.chathack.client;

import fr.upem.net.tcp.chathack.utils.context.ClientToClientContext;
import fr.upem.net.tcp.chathack.utils.context.ClientToServerContext;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

public class ChatHackClient {
    static private final int BUFFER_SIZE = 10_000;
    static private final int BLOCKING_QUEUE_SIZE = 100;
    static private final Logger logger = Logger.getLogger(ChatHackClient.class.getName());

    private final SocketChannel sc;
    private final ServerSocketChannel ssc;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private final InetSocketAddress localServerAddress;
    private String login;
    private String password;
    private final Thread console;
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    private Context clientToServerContext;
    private Context clientToClientContext;
    boolean isConnected = false;
    boolean wantADisconnection = false;
    //Récupérer le context pour savoir avec qui je suis connecté
    private final HashMap<String, Context> contextPrivateConnection = new HashMap<>();
    private final HashMap<String, ArrayBlockingQueue<String>> waitingMessage = new HashMap<>();
    private final ArrayList<String> refusedConnection = new ArrayList<>();
    private final ArrayBlockingQueue<PrivateConnectionFrame> connectionRequest = new ArrayBlockingQueue<>(BLOCKING_QUEUE_SIZE);
    private final ChatHackFrame frameLogin;
    private final HashMap<Long, String> requestWaiting = new HashMap<>(BLOCKING_QUEUE_SIZE);
    private long idRequest = 1;

    private ChatHackClient(String login, String password,InetSocketAddress serverAddress, ChatHackFrame frameLogin) throws IOException {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);

        this.ssc = ServerSocketChannel.open();
        ssc.bind(null);
        this.localServerAddress = new InetSocketAddress(ssc.socket().getInetAddress(),ssc.socket().getLocalPort());
        this.frameLogin = frameLogin;


    }
    public ChatHackClient(String login, InetSocketAddress serverAddress) throws IOException {
        this(login,"",serverAddress,ConnectionFrame.createConnectionFrame(0, login));
        LOGGER.log(Level.INFO, "creating login frame");
    }

    // constructor with login and password
    public ChatHackClient(String login, String password, InetSocketAddress serverAddress) throws IOException {
        this(login,password,serverAddress,LoginPasswordFrame.createLoginPasswordFrame(1, login, password));
        LOGGER.log(Level.INFO, "creating login with password frame");

    }

    public ChatHackFrame getFrameLogin() {
        return this.frameLogin;
    }

    public String getLogin() {
        return this.login;
    }

    private void consoleRun() {
        try {
            var scan = new Scanner(System.in);
            while (scan.hasNextLine()) {
                var msg = scan.nextLine();
                sendCommand(msg);
            }
        } catch (InterruptedException e) {
            logger.info("Console thread has been interrupted");
        } finally {
            logger.info("Console thread stopping");
        }
    }

    /**
     * Send a command to the selector via commandQueue and wake it up
     *
     * @param msg Command written by the user
     * @throws InterruptedException if the console thread is interrupted by the main thread
     */
    private void sendCommand(String msg) throws InterruptedException {
        synchronized (commandQueue) {
            commandQueue.put(msg);
            selector.wakeup();
        }
    }

    /**
     * Processes the command from commandQueue.
     */
    private void processCommands() {
        while (!commandQueue.isEmpty()) {
            synchronized (commandQueue) {
                var command = commandQueue.poll();
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                //private msg + file
                assert command != null;
                if (command.startsWith("/") || command.startsWith("@")) {
                    //Extraction du Login
                    var splitTab = command.split(" ", 2);
                    if (splitTab[0].length() < 1) {
                        //Login incorrect car inferieur à 1
                        break;
                    }
                    //Start with 1 for the first letter of the Login
                    var target = splitTab[0].substring(1);
                    //@Bob = message privé pour bob
                    if (command.startsWith("@")) {
                        //La suite de mon tab donc mon msg
                        String message;
                        if(splitTab.length == 1) {
                            message = "";
                        } else {
                            message = splitTab[1];
                        }
                        //Si on a une connexion privée déjà établie
                        if (contextPrivateConnection.containsKey(target)) {
                            var context = contextPrivateConnection.get(target);
                            var privateMessage = SimpleFrame.createSimpleFrame(PRIVATE_MESSAGE, message);
                            privateMessage.fillByteBuffer(buffer);
                            context.queueMessage(buffer);
                            System.out.println("Send to : " + target + " -> " + message );
                        } else if (refusedConnection.contains(target)) {
                            System.out.println("The client : " + target + " has already refused the connection");
                            return;
                        } else if (waitingMessage.containsKey(target)) {
                            //Cas ou le client a fait la demande de connexion mais n'a pas encore eu de réponse 'délais entre ok et ko de la part de client 2
                            var targetMessages = waitingMessage.get(target);
                            targetMessages.add(message);
                        } else {
                            // Pour la demande de connexion
                            var requestConnectionFrame = PrivateConnectionFrame
                                    .createPrivateConnectionFrame(PRIVATE_CONNECTION_REQUEST, target, idRequest, localServerAddress);
                            requestConnectionFrame.fillByteBuffer(buffer);
                            clientToServerContext.queueMessage(buffer);
                            var queue = new ArrayBlockingQueue<String>(BLOCKING_QUEUE_SIZE);
                            queue.add(message);
                            //Message stocké temporairement
                            waitingMessage.put(target, queue);

                            requestWaiting.put(idRequest, target);
                            idRequest++;
                        }
                    } else { // command starts with "/"
                        // /Bob = file pour bob
                        if(!contextPrivateConnection.containsKey(target)) {
                            System.out.println("Send a private message in order to create a private connection.");
                            break;
                        }
                        if(splitTab.length == 1) {
                            System.out.println("You must specify a file in order to send it.");
                            break;
                        }
                        String filePath = splitTab[1];
                        try(RandomAccessFile store = new RandomAccessFile(filePath, "r")) {
                            try(FileChannel fileChannel = store.getChannel()) {
                                // get name of file from the path name
                                int lastSlash;
                                String fileName;
                                if((lastSlash = filePath.lastIndexOf("/")) == -1) {
                                    fileName = filePath;
                                } else {
                                    fileName = filePath.substring(lastSlash+1);
                                }
                                long fileSize = fileChannel.size();
                                if(fileSize > 1_024) {
                                    System.out.println("File too large.");
                                    break;
                                }
                                ByteBuffer fileBuffer = ByteBuffer.allocate((int)fileSize);
                                fileChannel.read(fileBuffer);
                                fileBuffer.flip();
                                FileFrame fileFrame = FileFrame.createFileFrame(PRIVATE_FILE, fileName, fileBuffer);
                                Context context = contextPrivateConnection.get(target);
                                ByteBuffer privateFileBuffer = ByteBuffer.allocate(10_000);
                                fileFrame.fillByteBuffer(privateFileBuffer);
                                context.queueMessage(privateFileBuffer);
                            }
                        } catch (IOException e) {
                            System.out.println("file not found");
                            break;
                        }
                    }
                } else if (command.startsWith("&")) { // disconnection
                    var disconnection = ConnectionFrame.createConnectionFrame(DISCONNECTION_REQUEST, login);
                    disconnection.fillByteBuffer(buffer);
                    clientToServerContext.queueMessage(buffer);
                    for (SelectionKey key : selector.keys()) {
                        Context ctx = (Context) key.attachment();
                        if(ctx instanceof ClientToClientContext) {
                            ctx.silentlyClose();
                        }
                    }
                    //Accept or refuse connection client to client
                } else if (command.startsWith("$")) {
                    var command2 = command.substring(1);
                    if (command2.equals("accept") || command2.equals("refuse")) {
                        if (!connectionRequest.isEmpty()) {
                            PrivateConnectionFrame frame = connectionRequest.poll();
                            int opCode;
                            if (command2.equals("accept")) {
                                opCode = PRIVATE_CONNECTION_OK;
                                //Create new connection
                                createPrivateConnection(frame);
                            } else {
                                opCode = PRIVATE_CONNECTION_KO;
                            }
                            PrivateConnectionResponseFrame newFrame = PrivateConnectionResponseFrame.createPrivateConnectionResponseFrame(opCode, frame.getIdRequest());
                            newFrame.fillByteBuffer(buffer);
                            clientToServerContext.queueMessage(buffer);
                        }
                    } else {
                        System.out.println("This command is not recognized");
                    }
                } else {
                    //Broadcast
                    var broadcastMsg = GlobalMessageFrame.createGlobalMessageFrame(GLOBAL_MESSAGE, login, command);
                    broadcastMsg.fillByteBuffer(buffer);
                    clientToServerContext.queueMessage(buffer);
                }
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ChatHackClient.class.getName());

    public void launch() throws IOException {
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        sc.configureBlocking(false);
        sc.connect(serverAddress);
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        clientToServerContext = new ClientToServerContext(key, this);
        key.attach(clientToServerContext);


        while (!Thread.interrupted() && !wantADisconnection) {
            //printKeys();
            try {
                selector.select(this::treatKey);
                processCommands();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
    }

    public ArrayBlockingQueue<PrivateConnectionFrame> getConnectionRequest() {
        return connectionRequest;
    }

    public void createPrivateConnection(PrivateConnectionFrame frame) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(frame.getAddress());
            SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT);
            ClientToClientContext clientToClientContext = new ClientToClientContext(key, this);
            key.attach(clientToClientContext);
            contextPrivateConnection.put(frame.getLogin(), clientToClientContext);
            clientToClientContext.setLogin(frame.getLogin());
            ConnectionFrame presentationFrame = ConnectionFrame.createConnectionFrame(PRESENTATION_LOGIN, login);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            presentationFrame.fillByteBuffer(buffer);
            clientToClientContext.queueMessageWhithOutUpdateInterestOps(buffer);
        } catch (IOException ignored) {
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ssc.accept();
        if (sc == null) {
            return;
        }
        sc.configureBlocking(false);
        SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);
        var clientContext = new ClientToClientContext(scKey, this);
        scKey.attach(clientContext);
    }

    private void treatKey(SelectionKey key) {
        //printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isConnectable()) {
                //LOGGER.log(Level.INFO, "key is connectable");
                ((Context) key.attachment()).doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                //LOGGER.log(Level.INFO, "key is writable");
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                //LOGGER.log(Level.INFO, "key is readable");
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            ((Context) key.attachment()).silentlyClose();
        }
    }

    /***
     * Theses methods are here to help understanding the behavior of the selector
     ***/

    private String interestOpsToString(SelectionKey key) {
        if (!key.isValid()) {
            return "CANCELLED";
        }
        int interestOps = key.interestOps();
        ArrayList<String> list = new ArrayList<>();
        if ((interestOps & SelectionKey.OP_ACCEPT) != 0)
            list.add("OP_ACCEPT");
        if ((interestOps & SelectionKey.OP_READ) != 0)
            list.add("OP_READ");
        if ((interestOps & SelectionKey.OP_WRITE) != 0)
            list.add("OP_WRITE");
        return String.join("|", list);
    }

    public void printKeys() {
        Set<SelectionKey> selectionKeySet = selector.keys();
        if (selectionKeySet.isEmpty()) {
            System.out.println("The selector contains no key : this should not happen!");
            return;
        }
        System.out.println("The selector contains:");
        for (SelectionKey key : selectionKeySet) {
            SelectableChannel channel = key.channel();
            if (channel instanceof ServerSocketChannel) {
                System.out.println("\tKey for ServerSocketChannel : " + interestOpsToString(key));
            } else {
                SocketChannel sc = (SocketChannel) channel;
                System.out.println("\tKey for Client " + remoteAddressToString(sc) + " : " + interestOpsToString(key));
            }
        }
    }

    public void printSelectedKey(SelectionKey key) {
        SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
        } else {
            SocketChannel sc = (SocketChannel) channel;
            System.out.println(
                    "\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
        }
    }

    private String possibleActionsToString(SelectionKey key) {
        if (!key.isValid()) {
            return "CANCELLED";
        }
        ArrayList<String> list = new ArrayList<>();
        if (key.isAcceptable())
            list.add("ACCEPT");
        if (key.isReadable())
            list.add("READ");
        if (key.isWritable())
            list.add("WRITE");
        return String.join(" and ", list);
    }

    private String remoteAddressToString(SocketChannel sc) {
        try {
            return sc.getRemoteAddress().toString();
        } catch (IOException e) {
            return "???";
        }
    }

    private void silentlyClose() {
        try {
            ssc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    private static void usage() {
        System.out.println("Usage : ChatHackClient login hostname port\n" +
                "Usage with password : ChatHackClient login password hostname port");
    }

    public HashMap<String, Context> getContextPrivateConnection() {
        return contextPrivateConnection;
    }

    public HashMap<String, ArrayBlockingQueue<String>> getWaitingMessage() {
        return waitingMessage;
    }

    public ArrayList<String> getRefusedConnection() {
        return refusedConnection;
    }

    public HashMap<Long, String> getRequestWaiting() {
        return requestWaiting;
    }

    public boolean connected() {
        return isConnected;
    }

    public void setConnected() {
        console.start();
        isConnected = true;
    }

    public void stop() {
        console.interrupt();
        for (SelectionKey key : selector.keys()) {
            Context ctx = (Context) key.attachment();
            if (ctx == null) {
                continue;
            }
            ctx.silentlyClose();
        }
        silentlyClose();
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 3 && args.length != 4) {
            usage();
            return;
        }
        if (args.length == 3) {
            new ChatHackClient(args[0], new InetSocketAddress(args[1], Integer.parseInt(args[2]))).launch();
        } else {
            new ChatHackClient(args[0], args[1], new InetSocketAddress(args[2], Integer.parseInt(args[3]))).launch();
        }
    }
}
