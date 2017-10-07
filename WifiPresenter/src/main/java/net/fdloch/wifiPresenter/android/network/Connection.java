package net.fdloch.wifiPresenter.android.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean shallStop = new AtomicBoolean(false);

    public Connection(Socket socket) throws IOException {
        this(new BufferedReader(new InputStreamReader(socket.getInputStream())), new PrintWriter(socket.getOutputStream()), socket);
    }

    public Connection(BufferedReader in, PrintWriter out, Socket socket) {
        this.in = in;
        this.out = out;
        this.socket = socket;

        this.remoteIP = this.socket.getRemoteSocketAddress().toString();

        new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    try {
                        Connection.this.send(msgQueue.take());
                    } catch (InterruptedException e) {
                        log.debug("Could not retrieve element from msgQueue due to an interruption!");
                    }
                }
            }
        }.start();

        log.debug("New Connection instance created");
    }

    public String getRemoteIP() {
        return this.remoteIP;
    }

    public void setListener(ConnectionListener l) {
        this.listener = l;
    }

    //shall only be used via CommunicationLayer
    void enqueueMessage(String msg) {
        this.msgQueue.add(msg);
    }

    // This should only be called from the dequeueing thread - never by the UI thread (unless one wants Android to throw an Exception)
    void send(String msg) {
        if (shallStop.get()) {
            return;
        }

        this.out.println(msg);
        this.out.flush();

        log.debug("Message sent: " + msg);
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1];

        try {
            while (this.socket.isConnected() && !this.shallStop.get()) {
                if (!this.in.ready()) {
                    continue;
                }

                this.in.read(buf, 0, 1);

                if (buf[0] == '\n') {
                    this.fireOnMessage(builder.toString());
                    builder = new StringBuilder();
                } else {
                    builder.append(buf);
                }
            }
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

    public void close() throws IOException, InterruptedException {
        log.debug("Going to close socket...");

        shallStop.set(true);
        join();

        log.debug("Successfully closed socket!");
    }
}
