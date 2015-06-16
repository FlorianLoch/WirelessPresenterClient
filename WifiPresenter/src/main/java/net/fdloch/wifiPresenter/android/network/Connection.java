package net.fdloch.wifiPresenter.android.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by florian on 12.03.15.
 */
public class Connection extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Connection.class);
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private ConnectionListener listener;
    private String remoteIP;
    private boolean stop;

    public Connection(Socket socket) throws IOException {
        this(new BufferedReader(new InputStreamReader(socket.getInputStream())), new PrintWriter(socket.getOutputStream()), socket);
    }

    public Connection(BufferedReader in, PrintWriter out, Socket socket) {
        this.in = in;
        this.out = out;
        this.socket = socket;

        this.remoteIP = this.socket.getRemoteSocketAddress().toString();

        log.debug("New Connection instance created");
    }

    public String getRemoteIP() {
        return this.remoteIP;
    }

    public void setListener(ConnectionListener l) {
        this.listener = l;
    }

    public void send(String msg) {
        this.out.println(msg);
        this.out.flush();

        log.debug("Message sent: " + msg);
    }

    @Override
    public void run() {
        try {
            while (this.socket.isConnected() && !this.socket.isClosed()) {
                String input = this.in.readLine();

                if (input == null) {
                    this.socket.close();
                    continue;
                }

                this.fireOnMessage(input);
            }
            this.fireOnDisconnect();
        } catch (Exception e) {
            this.fireOnError(e);
        } finally {
            if (this.socket != null) try {
                this.in.close();
                this.out.close();
                this.socket.close();
            } catch (IOException e) {
                this.fireOnError(e);
            }

            this.fireOnDisconnect();
        }
    }

    private void fireOnError(Exception e) {
        this.listener.onError(e);
    }

    private void fireOnMessage(String msg) {
        log.debug("Received message: " + msg);
        this.listener.onMessage(msg);
    }

    private void fireOnDisconnect() {
        this.listener.onDisconnect();
    }

    public void close() throws IOException {
        log.debug("Going to close socket...");

        this.socket.close();
        this.in.close();
        this.out.close();

        log.debug("Successfully closed socket!");
    }
}
