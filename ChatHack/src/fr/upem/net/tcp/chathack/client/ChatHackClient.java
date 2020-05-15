package fr.upem.net.tcp.chathack.client;

import fr.upem.net.tcp.chathack.utils.context.ClientToServerContext;
import fr.upem.net.tcp.chathack.utils.context.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ChatHackClient {

    static private final int BUFFER_SIZE = 10_000;
    static private final Logger logger = Logger.getLogger(ChatHackClient.class.getName());

    private final SocketChannel sc;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private String login;
    private String password;
    private final Thread console;
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    private Context clientToServerContext;
    private Context clientToClientContext;
    boolean isConnected = false;

    public ChatHackClient(String login, InetSocketAddress serverAddress) throws IOException {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = ""; // empty string means that there is no password
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
    }

    // constructor with login and password
    public ChatHackClient(String login, String password, InetSocketAddress serverAddress) throws IOException {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
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
        synchronized (commandQueue){
            commandQueue.put(msg);
            selector.wakeup();
        }
    }

    /**
     * Processes the command from commandQueue
     */
    private void processCommands() {
        for(;;){
            synchronized (commandQueue){
                var msg = commandQueue.poll();
                if(msg==null){
                    return;
                }//Creation en fonction du login mdp ou juste login
                /*if(){

                }else{

                }*/

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


    /*
    static private class Context {

        private static final Logger logger = Logger.getLogger(Context.class.getName());

        final private SelectionKey key;
        final private SocketChannel sc;
        final private ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final private ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
        final private FrameReader frameReader = new FrameReader();
        private boolean inputClosed = false;

        private final ClientToServerFrameVisitor frameVisitor = new ClientToServerFrameVisitor(this);

        private Context(SelectionKey key) {
            this.key = key;
            this.sc = (SocketChannel) key.channel();
        }


        //TODO
        private void processIn() {
            FrameReader frameReader = new FrameReader();
            for(;;){
                Reader.ProcessStatus status = frameReader.process(inputBuffer);
                switch (status){
                    case ERROR:
                        silentlyClose();
                        return;
                    case REFILL:
                        return;
                    case DONE:
                        ChatHackFrame frame = frameReader.get();
                        frameReader.reset();
                        treatFrame(frame);
                        break;
                }
            }
        }

        private void treatFrame(ChatHackFrame frame) {
            frame.accept(frameVisitor);
        }


        private void queueMessage(ByteBuffer bb) {
            queue.add(bb);
            processOut();
            updateInterestOps();
        }


        private void processOut() {
            while (!queue.isEmpty()) {
                var bb = queue.peek();
                if (bb.remaining() <= outputBuffer.remaining()) {
                    queue.remove();
                    outputBuffer.put(bb);
                } else {
                    return;
                }
            }
        }



        private void updateInterestOps() {
            var interestOps = 0;
            if (!inputClosed && inputBuffer.hasRemaining()) {
                interestOps = interestOps | SelectionKey.OP_READ;
            }
            if (outputBuffer.position() != 0) {
                interestOps |= SelectionKey.OP_WRITE;
            }
            if (interestOps == 0) {
                silentlyClose();
                return;
            }

            try {
                key.interestOps(interestOps);
            } catch (CancelledKeyException kE) {
                logger.log(Level.INFO, "connection has been shut down by the server");
                silentlyClose();
            }
        }

        private void silentlyClose() {
            try {
                sc.close();
            } catch (IOException e) {
                // ignore exception
            }
        }


        private void doRead() throws IOException {
            if (sc.read(inputBuffer) == -1) {
                logger.log(Level.INFO, "closed before reading");
                inputClosed = true;
            }
            processIn();
            updateInterestOps();
        }



        private void doWrite() throws IOException {
            outputBuffer.flip();
            sc.write(outputBuffer);
            outputBuffer.compact();
            processOut();
            updateInterestOps();
        }

        public void doConnect() throws IOException {
            if(!sc.finishConnect()){
                return;
            }
            updateInterestOps();
        }
    }

     */
}
