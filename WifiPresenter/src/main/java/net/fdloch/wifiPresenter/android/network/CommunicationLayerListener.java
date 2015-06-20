package net.fdloch.wifiPresenter.android.network;

/**
 * Created by florian on 20.06.15.
 */
public interface CommunicationLayerListener extends ConnectionListener {

    //Handshake failed is covered by onError()

    void onHandshakeCompleted();

}
