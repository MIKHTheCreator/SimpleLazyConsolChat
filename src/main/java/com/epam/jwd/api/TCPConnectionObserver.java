package com.epam.jwd.api;

import com.epam.jwd.connection.TCPConnection;

public interface TCPConnectionObserver {

    void onConnection(TCPConnection connection);
    void onReceiveString(TCPConnection connection, String string);
    void onException(TCPConnection connection, Exception exception);
    void onDisconnect(TCPConnection connection);
}
