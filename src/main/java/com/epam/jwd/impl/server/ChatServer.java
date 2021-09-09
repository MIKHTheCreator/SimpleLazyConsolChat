package com.epam.jwd.impl.server;

import com.epam.jwd.connection.TCPConnection;
import com.epam.jwd.api.TCPConnectionObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionObserver {

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private static final Logger log = LogManager.getLogger(ChatServer.class);
    private static final String START_MESSAGE = "Server started";
    private static final String CONNECTION_FAILED_LOG_MESSAGE = "Connection failed";
    private static final String SENDING_MESSAGE_TO_ALL_LOG_MESSAGE = "Message sends: ";
    private static final String CONNECTED_CLIENT_MESSAGE = "Client connected: ";
    private static final String DISCONNECTED_CLIENT_MESSAGE = "Client disconnected: ";
    private static final String SYSTEM_MESSAGE_DELIMITER_BEGIN = "SYSTEM{\s";
    private static final String SYSTEM_MESSAGE_DELIMITER_END = "}SYSTEM";
    private static final String USER_MESSAGE_DELIMITER = ">> ";
    private static final String TCP_EXCEPTION_LOG_MESSAGE = "TCPException: ";
    private static final int PORT = 8080;

    public ChatServer() {
        log.debug(START_MESSAGE);

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try{
                    new TCPConnection(serverSocket.accept(), this);
                } catch(IOException e) {
                    log.error(CONNECTION_FAILED_LOG_MESSAGE, e);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

    @Override
    public void onConnection(TCPConnection connection) {
        connections.add(connection);
        sendMessageToAllUsers(SYSTEM_MESSAGE_DELIMITER_BEGIN + CONNECTED_CLIENT_MESSAGE
                + connection + SYSTEM_MESSAGE_DELIMITER_END);
    }

    @Override
    public void onReceiveString(TCPConnection connection, String string) {
        sendMessageToAllUsers(USER_MESSAGE_DELIMITER + string);
    }

    @Override
    public void onException(TCPConnection connection, Exception exception) {
        log.error(TCP_EXCEPTION_LOG_MESSAGE, exception);
    }

    @Override
    public void onDisconnect(TCPConnection connection) {
        connections.remove(connection);
        sendMessageToAllUsers(SYSTEM_MESSAGE_DELIMITER_BEGIN + DISCONNECTED_CLIENT_MESSAGE
                + connection + SYSTEM_MESSAGE_DELIMITER_END);
    }

    private void sendMessageToAllUsers(String msg) {
        log.debug(SENDING_MESSAGE_TO_ALL_LOG_MESSAGE + msg);
        connections.forEach(connection -> connection.sendMessage(msg));
    }
}
