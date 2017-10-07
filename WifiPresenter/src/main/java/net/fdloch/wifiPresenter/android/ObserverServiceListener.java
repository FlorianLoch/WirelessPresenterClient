package net.fdloch.wifiPresenter.android;

/**
 * Created by d059349 on 07.10.17.
 */

public interface ObserverServiceListener {

    void onError(Exception e);

    void onConnectionEstablished();

    void onDisconnect();
}
