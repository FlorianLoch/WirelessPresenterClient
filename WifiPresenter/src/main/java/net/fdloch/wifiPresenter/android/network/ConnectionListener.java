package net.fdloch.wifiPresenter.android.network;

/**
 * Created by florian on 12.03.15.
 */
public interface ConnectionListener {

    public void onMessage(String msg);

    public void onError(Exception e);

    public void onDisconnect();
}
