package net.fdloch.wifiPresenter.android;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import android.widget.Toast;
import net.fdloch.wifiPresenter.android.activities.ServerSelection;
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

    private Connection conn;
    private ConnectionListener listener = new ConnectionListener() {
        @Override
        public void onMessage(String msg) {}

        @Override
        public void onError(Exception e) {
            log.error("An error regarding server connection occurred!", e);
        }

        @Override
        public void onDisconnect() {
            log.info(String.format("Connection to server '%s' lost!", conn.getRemoteIP()));
//            Toast.makeText(ObserverService.this, "Connection to server lost...", Toast.LENGTH_LONG);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        log.info("Service started!");

        ServerAddress serverAddress = intent.getParcelableExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS);

        connectToServer(serverAddress);

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

        return START_NOT_STICKY;
    }


    private void connectToServer(final ServerAddress address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(address.getHost(), PORT);

                    conn = new Connection(s);
                    CommunicationLayer commLayer = new CommunicationLayer(listener, conn, address.getPasscode());

                    conn.start();

                    cP = new CommandProducer(commLayer);
                } catch (Exception ex) {
                    listener.onError(new Exception("Could not connect to server!", ex));
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
}
