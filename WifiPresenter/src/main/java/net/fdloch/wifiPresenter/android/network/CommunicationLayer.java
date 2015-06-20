package net.fdloch.wifiPresenter.android.network;

import net.fdloch.wifiPresenter.android.util.Hasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by florian on 14.03.15.
 */
public class CommunicationLayer implements ConnectionListener {
    private static final Logger log = LoggerFactory.getLogger(CommunicationLayer.class);
    private CommunicationLayerListener listener;
    private Connection conn;
    private boolean handshakeCompleted;
    private String passphrase;

    public CommunicationLayer(CommunicationLayerListener listener, Connection conn, String passphrase) {
        this.listener = listener;
        this.conn = conn;
        this.handshakeCompleted = false;
        this.passphrase = passphrase;

        this.conn.setListener(this);
    }

    public void send(String msg) {
        if (!this.handshakeCompleted) {
            log.info(String.format("Message '%s' has not been send because handshake hat not yet been completed.", msg));
            return;
        }

        this.conn.send(msg);
    }

    @Override
    public void onMessage(String msg) {
        if (handshakeCompleted) {
            this.listener.onMessage(msg);
            return;
        }

        if (msg.indexOf("hello") == 0) {
            try {
                String receivedNonce = msg.substring("hello".length());
                String hash = Hasher.computeHash(receivedNonce + this.passphrase);

                this.conn.send(hash);
            }
            catch (Exception e) {
                this.onError(new Exception("Could not compute hash for completing handshake", e));
            }
        }

        if (msg.equals("hs failed")) {
            handshakeCompleted = false;
            log.error("Error: Handshake with server failed (usually due to wrong passphrase)!");
            this.listener.onError(new HandshakeFailedException());
        }

        if (msg.equals("hs successful")) {
            log.info("Handshake successfully completed!");
            handshakeCompleted = true;
            this.listener.onHandshakeCompleted();
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("Error received from underlying Connection instance:", e);
        this.listener.onError(e);
    }

    @Override
    public void onDisconnect() {
        this.listener.onDisconnect();
    }
}
