package net.fdloch.wifiPresenter.android;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by florian on 17.06.15.
 */
public class SoundButtonService extends Service {
    private static final Logger log = LoggerFactory.getLogger(SoundButtonService.class);
    private SoundButtonObserver soundButtonObserver;
    private boolean isRunning;
    private Thread audioProducer;

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
            }
        });
        this.soundButtonObserver.setOnVolumeUpListener(new SoundButtonObserver.SoundButtonListener() {
            @Override
            public void onButtonPressed() {
                log.info("Volume up");
            }
        });
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this.soundButtonObserver);

        log.info("Service started!");

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



    @Override
    public void onDestroy() {
        super.onDestroy();

        getApplicationContext().getContentResolver().unregisterContentObserver(this.soundButtonObserver);
        isRunning = false;
        try {
            this.audioProducer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.audioProducer = null;
        log.info("Service gets destroyed");
    }
}
