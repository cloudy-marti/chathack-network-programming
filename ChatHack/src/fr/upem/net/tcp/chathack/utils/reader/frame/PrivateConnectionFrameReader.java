package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.PrivateConnectionFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.IntReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.IpAddressReader;
import fr.upem.net.tcp.chathack.utils.reader.utils.Reader;
import fr.upem.net.tcp.chathack.utils.reader.utils.StringReader;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PrivateConnectionFrameReader implements Reader<PrivateConnectionFrame> {

    private enum State {
        DONE, WAITING_LOGIN, WAITING_ADDRESS, WAITING_PORT, ERROR
    }

    private State state = State.WAITING_LOGIN;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String login;
    private ByteBuffer address;
    private int port;

    /*
                int       String        byte         byte     int
           --------------------------------------------------------
           | SizeOfLogin | Login | SizeOfAddress | Address | Port |
           --------------------------------------------------------
     */
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        StringReader stringReader = new StringReader();
        if(state == State.WAITING_LOGIN) {
            ProcessStatus status = stringReader.process(buffer);
            switch (status) {
                case DONE:
                    login = stringReader.get();
                    state = State.WAITING_ADDRESS;
                    break;
                case REFILL:
                    return ProcessStatus.REFILL;
                case ERROR:
                    return ProcessStatus.ERROR;
            }
        }

        IpAddressReader ipAddressReader = new IpAddressReader();
        ProcessStatus status = ipAddressReader.process(buffer);
        switch (status) {
            case DONE:
                address = ipAddressReader.get();
                state = State.WAITING_PORT;
                break;
            case REFILL:
                return ProcessStatus.REFILL;
            case ERROR:
                return ProcessStatus.ERROR;
        }

        IntReader intReader = new IntReader();
        status = intReader.process(buffer);
        switch (status) {
            case DONE:
                port = intReader.get();
                state = State.DONE;
                break;
            case REFILL:
                return ProcessStatus.REFILL;
            case ERROR:
                return ProcessStatus.ERROR;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public PrivateConnectionFrame get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrivateConnectionFrame get(int opcode) {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        try {
            System.out.println("requesting to : " + login +
                    " with address : " + Arrays.toString(address.array()));
            return PrivateConnectionFrame.createPrivateConnectionFrame(opcode, login,
                    new InetSocketAddress(InetAddress.getByAddress(address.array()), port));
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void reset() {
        state = State.WAITING_LOGIN;
        internalBuffer.clear();
    }
}
