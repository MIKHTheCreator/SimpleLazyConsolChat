package com.epam.jwd.connection;

import com.epam.jwd.api.TCPConnectionObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TCPConnection {

    private final Socket socket;
    private final Thread thread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionObserver eventObserver;

    private static final Logger log = LogManager.getLogger(TCPConnection.class);

    public TCPConnection(Socket socket, TCPConnectionObserver eventObserver) throws IOException {
        this.eventObserver = eventObserver;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventObserver.onConnection(TCPConnection.this);
                    while (!thread.isInterrupted()) {
                        eventObserver.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    eventObserver.onException(TCPConnection.this, e);
                } finally {
                    eventObserver.onDisconnect(TCPConnection.this);
                }
            }
        });
        thread.start();
    }

    public TCPConnection(TCPConnectionObserver eventObserver, String ipAddress, int port)
            throws IOException {
        this(new Socket(ipAddress, port), eventObserver);
    }

    public synchronized void sendMessage(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventObserver.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventObserver.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return new StringBuffer("TCPConnection: ")
                .append("User with ipAddress: ")
                .append(socket.getInetAddress())
                .append(" with port: ")
                .append(socket.getPort())
                .toString();
    }
}
