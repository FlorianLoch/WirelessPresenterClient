package net.fdloch.wifiPresenter.android.network;

/**
 * Created by florian on 19.06.15.
 */
public class HandshakeFailedException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Handshake failed! Wrong password?";
    }
}
