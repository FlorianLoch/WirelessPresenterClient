package net.fdloch.wifiPresenter.android.network;

/**
 * Created by florian on 12.03.15.
 */
public interface ConnectionListener {
    void onMessage(String msg);

    void onError(Exception e);

    void onDisconnect();
}
