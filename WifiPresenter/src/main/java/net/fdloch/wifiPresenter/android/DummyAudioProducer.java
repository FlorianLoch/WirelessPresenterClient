package net.fdloch.wifiPresenter.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by d059349 on 07.10.17.
 */

class DummyAudioProducer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(DummyAudioProducer.class);
    private AtomicBoolean shallStop = new AtomicBoolean(false);

    @Override
    public void run() {
            int samplingRate = 44100;

            int bufSize = AudioTrack.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

            AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

            track.play();

            short samples[] = new short[bufSize];
            int amp = 1;
            double twopi = 8.0 * Math.atan(1.0);
            double fr = 440.0f;
            double ph = 0.0;

            log.info("Start playing music!");

            while (!shallStop.get()) {
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

    public void halt() {
        shallStop.set(true);
    }
}
