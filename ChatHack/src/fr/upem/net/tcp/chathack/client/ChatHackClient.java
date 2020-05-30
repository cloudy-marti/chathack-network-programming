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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.upem.net.tcp.chathack.utils.frame.ChatHackFrame.*;

/**
 * TCP Non-blocking client that follows the ChatHack protocol
 */
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
    private final Thread console;
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    private Context clientToServerContext;
    boolean isConnected = false;
    boolean wantADisconnection = false;
    // Retrieve context to know with whom this is privately connected
    private final HashMap<String, Context> contextPrivateConnection = new HashMap<>();
    private final HashMap<String, ArrayBlockingQueue<String>> waitingMessage = new HashMap<>();
    private final ArrayList<String> refusedConnection = new ArrayList<>();
    private final ArrayBlockingQueue<PrivateConnectionFrame> connectionRequest = new ArrayBlockingQueue<>(BLOCKING_QUEUE_SIZE);
    private final ChatHackFrame frameLogin;
    private final HashMap<Long, String> requestWaiting = new HashMap<>(BLOCKING_QUEUE_SIZE);
    private long idRequest = 1;
    private final String path;

    private ChatHackClient(String login, InetSocketAddress serverAddress, ChatHackFrame frameLogin, String path) throws IOException {
        this.serverAddress = serverAddress;
        this.login = login;
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.ssc = ServerSocketChannel.open();
        ssc.bind(null);
        this.localServerAddress = new InetSocketAddress(ssc.socket().getInetAddress(), ssc.socket().getLocalPort());
        this.frameLogin = frameLogin;
        this.path = path;
    }

    public ChatHackClient(String login, InetSocketAddress serverAddress, String path) throws IOException {
        this(login, serverAddress, ConnectionFrame.createConnectionFrame(0, login), path);
    }

    // constructor with login and password
    public ChatHackClient(String login, String password, InetSocketAddress serverAddress, String path) throws IOException {
        this(login, serverAddress, LoginPasswordFrame.createLoginPasswordFrame(1, login, password), path);
    }

    public ChatHackFrame getFrameLogin() {
        return this.frameLogin;
    }

    public String getLogin() {
        return this.login;
    }

    /**
     * Console thread's runnable that will allow the client to send commands and messages to ChatHack
     */
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
     * Processes the command from commandQueue. The commands are the following :
     * <b>message</b> : Message to be sent on global chat
     * <b>@login message</b> : Message to be sent on private to another connected client
     * <b>$accept/$refuse</b> : Accept or refuse private connection request to another connected client
     * <b>/login path_of_file</b> : File to be sent on private to another connected client
     * <b>&</b> : Disconnection request
     */
    private void processCommands() {
        while (!commandQueue.isEmpty()) {
            synchronized (commandQueue) {
                var command = commandQueue.poll();
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                //private msg + file
                assert command != null;
                if (command.startsWith("/") || command.startsWith("@")) {
                    // get login
                    var splitTab = command.split(" ", 2);
                    if (splitTab[0].length() < 1) {
                        // empty login is a wrong login
                        break;
                    }
                    //Start with 1 for the first letter of the Login
                    var target = splitTab[0].substring(1);
                    if (target.equals(login)) {
                        System.out.println("You can't send private messages to yourself !");
                        break;
                    }
                    if (command.startsWith("@")) { // private message
                        // next String of the array is the message
                        String message;
                        if (splitTab.length == 1) {
                            message = "";
                        } else {
                            message = splitTab[1];
                        }
                        // There is already a private connection
                        if (contextPrivateConnection.containsKey(target)) {
                            var context = contextPrivateConnection.get(target);
                            var privateMessage = SimpleFrame.createSimpleFrame(PRIVATE_MESSAGE, message);
                            privateMessage.fillByteBuffer(buffer);
                            context.queueMessage(buffer);
                            System.out.println("Send to : " + target + " -> " + message);
                        } else if (refusedConnection.contains(target)) {
                            System.out.println("The client : " + target + " has already refused the connection");
                            return;
                        } else if (waitingMessage.containsKey(target)) {
                            // if the target client does not respond within a certain delay
                            var targetMessages = waitingMessage.get(target);
                            targetMessages.add(message);
                        } else {
                            // Connection request
                            var requestConnectionFrame = PrivateConnectionFrame
                                    .createPrivateConnectionFrame(PRIVATE_CONNECTION_REQUEST, target, idRequest,
                                            localServerAddress);
                            requestConnectionFrame.fillByteBuffer(buffer);
                            clientToServerContext.queueMessage(buffer);
                            var queue = new ArrayBlockingQueue<String>(BLOCKING_QUEUE_SIZE);
                            queue.add(message);
                            // Message kept for a certain time
                            waitingMessage.put(target, queue);
                            requestWaiting.put(idRequest, target);
                            idRequest++;
                        }
                    } else { // command starts with "/"
                        if (!contextPrivateConnection.containsKey(target)) {
                            System.out.println("Send a private message in order to create a private connection.");
                            break;
                        }
                        if (splitTab.length == 1) {
                            System.out.println("You must specify a file in order to send it.");
                            break;
                        }
                        String filePath = splitTab[1];
                        try (RandomAccessFile store = new RandomAccessFile(path + filePath, "r")) {
                            try (FileChannel fileChannel = store.getChannel()) {
                                // get name of file from the path name
                                int lastSlash;
                                String fileName;
                                if ((lastSlash = filePath.lastIndexOf("/")) == -1) {
                                    fileName = filePath;
                                } else {
                                    fileName = filePath.substring(lastSlash + 1);
                                }
                                long fileSize = fileChannel.size();
                                if (fileSize > 1_024) {
                                    System.out.println("File too large.");
                                    break;
                                }
                                ByteBuffer fileBuffer = ByteBuffer.allocate((int) fileSize);
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
                        if (ctx instanceof ClientToClientContext) {
                            ctx.silentlyClose();
                        }
                    }
                    // Accept or refuse connection client to client
                } else if (command.startsWith("$")) {
                    var command2 = command.substring(1);
                    if (command2.equals("accept") || command2.equals("refuse")) {
                        if (!connectionRequest.isEmpty()) {
                            PrivateConnectionFrame frame = connectionRequest.poll();
                            int opCode;
                            if (command2.equals("accept")) {
                                opCode = PRIVATE_CONNECTION_OK;
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

    /**
     * Initialize and launch the client in non-blocking mode
     * @throws IOException if one of the socket channel or the selector methods fails
     */
    public void launch() throws IOException {
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        sc.configureBlocking(false);
        sc.connect(serverAddress);
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        clientToServerContext = new ClientToServerContext(key, this);
        key.attach(clientToServerContext);

        while (!Thread.interrupted() && !wantADisconnection) {
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

    /**
     * Connect to a channel opened by client who wants a private connection
     * And send a presentation frame to inform that the connection is OK
     * @param frame private connection frame that contains the address to be connected to
     */
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
            clientToClientContext.queueMessageWithoutUpdateInterestOps(buffer);
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
                ((Context) key.attachment()).doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            ((Context) key.attachment()).silentlyClose();
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
        System.out.println("Usage : ChatHackClient login hostname port path_of_files\n" +
                "Usage with password : ChatHackClient login password hostname port path_of_files");
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

    /**
     * Close all active private connections and the client itself.
     */
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
        if (args.length != 4 && args.length != 5) {
            usage();
            return;
        }
        if (args.length == 4) {
            new ChatHackClient(args[0], new InetSocketAddress(args[1], Integer.parseInt(args[2])), args[3]).launch();
        } else {
            new ChatHackClient(args[0], args[1], new InetSocketAddress(args[2], Integer.parseInt(args[3])), args[4]).launch();
        }
    }

    public String getPath() {
        return path;
    }
}
