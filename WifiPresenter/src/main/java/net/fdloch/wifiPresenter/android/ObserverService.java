package net.fdloch.wifiPresenter.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import net.fdloch.wifiPresenter.android.activities.ControlActivity;
import net.fdloch.wifiPresenter.android.activities.ServerSelection;
import net.fdloch.wifiPresenter.android.network.CommunicationLayerListener;
import net.fdloch.wifiPresenter.android.network.Connection;
import net.fdloch.wifiPresenter.android.network.ConnectionListener;
import net.fdloch.wifiPresenter.android.network.CommunicationLayer;
import net.fdloch.wifiPresenter.android.types.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by florian on 17.06.15.
 */
public class ObserverService extends Service {
    private static final Logger log = LoggerFactory.getLogger(ObserverService.class);
    private SoundButtonObserver soundButtonObserver;
    private DummyAudioProducer audioProducer;
    private CommandProducer cP;
    public static final int PORT = 8081;
    private ObserverServiceListener observerListener;
    private Connection conn;
    private CommunicationLayerListener communicationListener = new CommunicationLayerListener() {
        @Override
        public void onHandshakeCompleted() {
            observerListener.onConnectionEstablished();
        }

        @Override
        public void onMessage(String msg) {
            // NOOP; we do not need to handle incoming messages from the server
        }

        @Override
        public void onError(Exception e) {
            observerListener.onError(e);
        }

        @Override
        public void onDisconnect() {
            log.info(String.format("Connection to server '%s' lost!", conn.getRemoteIP()));
            observerListener.onDisconnect();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        setupNotification();

        this.soundButtonObserver = new SoundButtonObserver(this, new Handler());
        this.soundButtonObserver.setOnVolumeDownListener(new SoundButtonObserver.SoundButtonListener() {
            @Override
            public void onButtonPressed() {
                log.info("Volume down!");
                cP.fireNextCommand();
            }
        });
        this.soundButtonObserver.setOnVolumeUpListener(new SoundButtonObserver.SoundButtonListener() {
            @Override
            public void onButtonPressed() {
                log.info("Volume up");
                cP.fireBackCommand();
            }
        });

        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this.soundButtonObserver);

        this.audioProducer = new DummyAudioProducer();
        this.audioProducer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        log.info("Service bound!");

        return new ObeserverServiceBinder();
    }

    public class ObeserverServiceBinder extends Binder {
        public ObserverService getService() {
            return ObserverService.this;
        }
    }

    public void setObserverListener(ObserverServiceListener observerListener) {
        this.observerListener = observerListener;
    }

    public void connect(final ServerAddress address) {
        // needed because creating a socket counts as a network operation (on the main thread)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(address.getHost(), PORT);

                    conn = new Connection(s);
                    CommunicationLayer commLayer = new CommunicationLayer(communicationListener, conn, address.getPasscode());

                    conn.start();

                    cP = new CommandProducer(commLayer);
                } catch (Exception ex) {
                    observerListener.onError(new Exception("Could not connect to server!", ex));
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getApplicationContext().getContentResolver().unregisterContentObserver(this.soundButtonObserver);
        this.audioProducer.halt();
        try {
            this.audioProducer.join();
        } catch (InterruptedException e) {
            log.warn("Failed to close audioProducer!", e);
            e.printStackTrace();
        }

        disconnect();

        log.info("Service gets destroyed");
    }

    public void disconnect() {
        // TODO Refactor this, move code to Connection class
        try {
            if (this.conn != null) {
                this.conn.close();
                this.conn.join();
            }
        } catch (IOException e) {
            log.warn("Failed to close connection!", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException occured during shutdown of connection", e);
        }
    }

    private void setupNotification() {
        Intent notificationIntent = new Intent(this, ControlActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_details))
                .setContentIntent(pendingIntent);

        startForeground(28, builder.build());
    }
}
