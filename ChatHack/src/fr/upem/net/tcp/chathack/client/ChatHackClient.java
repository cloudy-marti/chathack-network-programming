package fr.upem.net.tcp.chathack.client;

import fr.upem.net.tcp.chathack.utils.context.ClientToServerContext;
import fr.upem.net.tcp.chathack.utils.context.Context;
import fr.upem.net.tcp.chathack.utils.frame.GlobalMessageFrame;
import fr.upem.net.tcp.chathack.utils.frame.PrivateConnectionFrame;
import fr.upem.net.tcp.chathack.utils.frame.SimpleFrame;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ChatHackClient {

    static private final int BUFFER_SIZE = 10_000;
    static private final int BLOCKING_QUEUE_SIZE = 100;
    static private final Logger logger = Logger.getLogger(ChatHackClient.class.getName());

    private final SocketChannel sc;
    private final ServerSocketChannel ssc;
    private final int port;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private String login;
    private String password;
    private final Thread console;
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    private Context clientToServerContext;
    private Context clientToClientContext;
    boolean isConnected = false;
    //Récupérer le context pour savoir avec qui je suis connecté
    private final HashMap<String, Context> contextPrivateConnection = new HashMap<>();
    private final HashMap<String, ArrayBlockingQueue<String>> waitingMessage = new HashMap<>();
    private final ArrayList<String> refusedConnection = new ArrayList<>();


    public ChatHackClient(String login, InetSocketAddress serverAddress, int port) throws IOException {
        this(login, "", serverAddress, port);
    }

    // constructor with login and password
    public ChatHackClient(String login, String password, InetSocketAddress serverAddress, int port) throws IOException {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.port = port;
        this.ssc = ServerSocketChannel.open();
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
     * @param msg
     * @throws InterruptedException
     */
    private void sendCommand(String msg) throws InterruptedException {
        synchronized (commandQueue) {
            commandQueue.put(msg);
            selector.wakeup();
        }
    }

    /**
     * Processes the command from commandQueue
     */
    private void processCommands() {
        while (!commandQueue.isEmpty()) {
            synchronized (commandQueue) {
                var commmand = commandQueue.poll();
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                //private msg + file
                if (commmand.startsWith("/") || commmand.startsWith("@")) {
                    //Extraction du Login
                    var splitTab = commmand.split(" ", 2);
                    if (splitTab[0].length() < 1) {
                        //Login incorrect car inferieur à 1
                        break;
                    }
                    //Start with 1 for the first letter of the Login
                    var target = splitTab[0].substring(1);
                    //@Bob = message privé pour bob
                    if (commmand.startsWith("@")) {
                        //La suite de mon tab donc mon msg
                        var message = splitTab[1];
                        //Si on a une connexion privée déjà établie
                        if (contextPrivateConnection.containsKey(target)) {
                            var context = contextPrivateConnection.get(target);
                            var privateMessage = SimpleFrame.createSimpleFrame(21, message);
                            privateMessage.fillByteBuffer(buffer);
                            context.queueMessage(buffer);
                        } else if (refusedConnection.contains(target)) {
                            System.out.println("The client : " + target + " as already refused the connection");
                            return;
                        } else if (waitingMessage.containsKey(target)) {
                            //Cas ou le client a fait la demande de connexion mais n'a pas encore eu de réponse 'délais entre ok et ko de la part de client 2
                            var targetMessages = waitingMessage.get(target);
                            targetMessages.add(message);
                        } else {
                            // Pour la demande de connexion
                            var requestConnectionFrame = PrivateConnectionFrame.createPrivateConnectionFrame(2, target, serverAddress);
                            requestConnectionFrame.fillByteBuffer(buffer);
                            clientToServerContext.queueMessage(buffer);
                            var queue = new ArrayBlockingQueue<String>(BLOCKING_QUEUE_SIZE);
                            queue.add(message);
                            //Message stocker temporairement
                            waitingMessage.put(target, queue);
                        }
                    } else {
                        // /Bob file pour bob

                    }
                } else {
                    //Broadcast
                    var broadcastMsg = GlobalMessageFrame.createGlobalMessageFrame(20, login, commmand);
                    broadcastMsg.fillByteBuffer(buffer);
                    clientToServerContext.queueMessage(buffer);
                }
            }
        }
    }

    public void launch() throws IOException {
        sc.configureBlocking(false);
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        clientToServerContext = new ClientToServerContext(key);
        key.attach(clientToServerContext);
        sc.connect(serverAddress);
        console.start();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));
        while (!Thread.interrupted()) {
            try {
                selector.select(this::treatKey);
                processCommands();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
    }

    private void treatKey(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                clientToServerContext.doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                clientToServerContext.doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                clientToServerContext.doRead();
            }
        } catch (IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
    }


    private void silentlyClose(SelectionKey key) {
        Channel sc = key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    private static void usage() {
        System.out.println("Usage : ChatHackClient login hostname port\n" +
                "Usage with password : ChatHackClient login password hostname port");
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 4 && args.length != 5) {
            usage();
            return;
        }
        if (args.length == 3) {
            new ChatHackClient(args[0], new InetSocketAddress(args[1], Integer.parseInt(args[2])), Integer.parseInt(args[3])).launch();
        } else {
            new ChatHackClient(args[0], args[1], new InetSocketAddress(args[2], Integer.parseInt(args[3])), Integer.parseInt(args[4])).launch();
        }
    }
}
