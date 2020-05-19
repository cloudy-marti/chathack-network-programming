package fr.upem.net.tcp.chathack.utils.frame;

import fr.upem.net.tcp.chathack.utils.visitor.FrameVisitor;

import java.nio.ByteBuffer;

public interface ChatHackFrame {
    void fillByteBuffer(ByteBuffer bbdst);
    boolean checkBufferSize(ByteBuffer buffer);
    void accept(FrameVisitor visitor);
    int getOpcode();

    // constant opcodes
    int CONNECTION_WITH_LOGIN = 0;
    int CONNECTION_WITH_LOGIN_AND_PASSWORD = 1;
    int PRIVATE_CONNECTION_REQUEST = 2;
    int DISCONNECTION_REQUEST = 3;
    int PRESENTATION_LOGIN = 4;
    int CONNECTION_WITH_LOGIN_OK = 10;
    int CONNECTION_WITH_LOGIN_AND_PASSWORD_OK = 11;
    int CONNECTION_KO = 12;
    int PRIVATE_CONNECTION_OK = 13;
    int PRIVATE_CONNECTION_KO = 14;
    int DISCONNECTION_OK = 15;
    int GLOBAL_MESSAGE = 20;
    int PRIVATE_MESSAGE = 21;
    int PRIVATE_FILE = 22;
    int LOGIN_ERROR = 30;
    int LOGIN_WITH_PASSWORD_ERROR = 31;
    int INVALID_ADDRESS = 32;
    int INVALID_PORT = 33;
    int DISCONNECTION_KO = 34;
}
