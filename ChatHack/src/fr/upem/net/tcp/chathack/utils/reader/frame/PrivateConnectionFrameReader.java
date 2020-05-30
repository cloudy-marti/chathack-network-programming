package fr.upem.net.tcp.chathack.utils.reader.frame;

import fr.upem.net.tcp.chathack.utils.frame.PrivateConnectionFrame;
import fr.upem.net.tcp.chathack.utils.reader.utils.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class PrivateConnectionFrameReader implements Reader<PrivateConnectionFrame> {

    private enum State {
        DONE, WAITING_LOGIN, WAITING_ADDRESS, WAITING_PORT, ERROR, WAITING_ID_REQUEST
    }

    private State state = State.WAITING_LOGIN;
    private static final int BUFFER_SIZE = 1_024;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode

    private String login;
    private ByteBuffer address;
    private int port;
    private long idRequest;
    private final StringReader stringReader = new StringReader();
    private final IpAddressReader ipAddressReader = new IpAddressReader();
    private final IntReader intReader = new IntReader();
    private final LongReader longReader = new LongReader();

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        ProcessStatus status;
        switch (state) {
            case WAITING_LOGIN:
                status = stringReader.process(buffer);
                switch (status) {
                    case DONE:
                        login = stringReader.get();
                        state = State.WAITING_ADDRESS;
                        break;
                    case REFILL:
                        return ProcessStatus.REFILL;
                    case ERROR:
                        state = State.ERROR;
                        return ProcessStatus.ERROR;
                }
            case WAITING_ADDRESS:
                status = ipAddressReader.process(buffer);
                switch (status) {
                    case DONE:
                        address = ipAddressReader.get();
                        state = State.WAITING_PORT;
                        break;
                    case REFILL:
                        return ProcessStatus.REFILL;
                    case ERROR:
                        state = State.ERROR;
                        return ProcessStatus.ERROR;
                }
            case WAITING_PORT:
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
            case WAITING_ID_REQUEST:
                status = longReader.process(buffer);
                switch (status) {
                    case DONE:
                        idRequest = longReader.get();
                        state = State.DONE;
                        break;
                    case REFILL:
                        return ProcessStatus.REFILL;
                    case ERROR:
                        return ProcessStatus.ERROR;
                }
                break;
            default:
                throw new IllegalStateException();
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
            return PrivateConnectionFrame.createPrivateConnectionFrame(opcode, login, idRequest,
                    new InetSocketAddress(InetAddress.getByAddress(address.array()), port));
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void reset() {
        state = State.WAITING_LOGIN;
        stringReader.reset();
        intReader.reset();
        longReader.reset();
        ipAddressReader.reset();
        internalBuffer.clear();
    }
}
