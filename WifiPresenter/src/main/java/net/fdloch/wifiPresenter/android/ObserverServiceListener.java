package net.fdloch.wifiPresenter.android;

/**
 * Created by florian on 20.06.15.
 */
public interface ObserverServiceListener {

    void onError(Exception e);

    void onConnectionEstablished();

}
