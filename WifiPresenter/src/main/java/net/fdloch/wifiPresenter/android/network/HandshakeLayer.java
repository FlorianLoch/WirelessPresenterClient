package net.fdloch.wifiPresenter.android.network;

import net.fdloch.wifiPresenter.android.util.Hasher;

/**
 * Created by florian on 14.03.15.
 */
public class HandshakeLayer implements ConnectionListener {
    private ConnectionListener listener;
    private Connection conn;
    private boolean handshakeCompleted;
    private String passphrase;

    public HandshakeLayer(ConnectionListener listener, Connection conn, String passphrase) {
        this.listener = listener;
        this.conn = conn;
        this.handshakeCompleted = false;
        this.passphrase = passphrase;

        this.conn.setListener(this);
    }

    @Override
    public void onMessage(String msg) {
        if (handshakeCompleted) {
            this.listener.onMessage(msg);
            return;
        }

        try {
            String hash = Hasher.computeHash(msg + this.passphrase);

            this.conn.send(hash);

            handshakeCompleted = true;
        }
        catch (Exception e) {
            this.onError(new Exception("Could not compute hash for completing handshake", e));
        }
    }

    @Override
    public void onError(Exception e) {
        this.listener.onError(e);
    }

    @Override
    public void onDisconnect() {
        this.listener.onDisconnect();
    }
}
