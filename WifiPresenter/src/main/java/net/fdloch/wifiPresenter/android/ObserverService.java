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
    private boolean isRunning;
    private Thread audioProducer;
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
        public void onMessage(String msg) {}

        @Override
        public void onError(Exception e) {
            observerListener.onError(e);
        }

        @Override
        public void onDisconnect() {
            //TODO special excpetion type for ConnectionLost
            observerListener.onError(new Exception(String.format("Connection to server '%s' lost!", conn.getRemoteIP())));
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        log.info("Service: Service bound!");

        return new ObeserverServiceBinder();
    }

    private void connectToServer(final ServerAddress address) {
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
                    communicationListener.onError(new Exception("Could not connect to server!", ex));
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getApplicationContext().getContentResolver().unregisterContentObserver(this.soundButtonObserver);
        isRunning = false;
        try {
            this.audioProducer.join();
        } catch (InterruptedException e) {
            log.warn("Failed to close audioProducer!", e);
            e.printStackTrace();
        }

        try {
            this.conn.close();
            this.conn.join();
        } catch (IOException e) {
            log.warn("Failed to close connection!", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.audioProducer = null;
        log.info("Service gets destroyed");
    }

    public class ObeserverServiceBinder extends Binder {
        public void setListener(ObserverServiceListener listener) {
            ObserverService.this.observerListener = listener;
        }

        public void initialize(ServerAddress serverAddress) {
            ObserverService.this.initialize(serverAddress);
        }
    }

    private void initialize(ServerAddress serverAddress) {
        setupNotification();

        connectToServer(serverAddress);

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


        this.audioProducer = new Thread(new Runnable() {
            @Override
            public void run() {
                int samplingRate = 44100;

                int bufSize = AudioTrack.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

                track.play();

                short samples[] = new short[bufSize];
                int amp = 1;
                double twopi = 8.*Math.atan(1.);
                double fr = 440.f;
                double ph = 0.0;

                log.info("Start playing music!");

                while(isRunning){
                    for(int i=0; i < bufSize; i++){
                        samples[i] = (short) (amp*Math.sin(ph));
                        ph += twopi*fr/samplingRate;
                    }
                    track.write(samples, 0, bufSize);
                }

                track.stop();
                track.release();
                log.info("Stopped playing audio");
            }
        });
        this.isRunning = true;
        this.audioProducer.start();
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
