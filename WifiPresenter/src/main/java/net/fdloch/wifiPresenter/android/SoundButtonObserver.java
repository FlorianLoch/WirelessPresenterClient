package net.fdloch.wifiPresenter.android;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by florian on 14.06.15.
 */
public class SoundButtonObserver extends ContentObserver {
    private static final Logger log = LoggerFactory.getLogger(SoundButtonObserver.class);
    private int previousVolume;
    private AudioManager audioMngr;
    private SoundButtonListener volumeUpListener;
    private SoundButtonListener volumeDownListener;
    private boolean active = false;

    public SoundButtonObserver(Context context, Handler handler) {
        super(handler);

        this.audioMngr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.previousVolume = getCurrentVolume();

        SoundButtonListener noop = new SoundButtonListener() {
            @Override
            public void onButtonPressed() {}
        };
        this.volumeUpListener = noop;
        this.volumeDownListener = noop;

        int newValue = getMaxVolume() / 2;
        this.previousVolume = newValue;
        setAudioVolume(newValue);

        this.active = true;
    }

    public void setOnVolumeUpListener(SoundButtonListener listener) {
        this.volumeUpListener = listener;
    }

    public void setOnVolumeDownListener(SoundButtonListener listener) {
        this.volumeDownListener = listener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        int maxVolume = getMaxVolume();
        int currentVolume = getCurrentVolume();

        log.info("Changed volume to " + currentVolume);

        int delta = this.previousVolume - currentVolume;
        this.previousVolume = currentVolume;

        //Delta of 0 occurs when volume gets placed in the middle because this.previousVolume gets set before event is fired so delta is zero.
        //This case shall be ignored!
        if (delta < 0) {
            this.volumeUpListener.onButtonPressed();
        }
        else if (delta > 0) {
            this.volumeDownListener.onButtonPressed();
        }

        if (0 == currentVolume || maxVolume == currentVolume) {
            int newVolume = maxVolume / 2;
            this.previousVolume = newVolume;
            setAudioVolume(newVolume);
        }
    }

    private void setAudioVolume(int volume) {
        this.audioMngr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        log.info("Set volume of 'STREAM_MUSIC' to: " + volume);
    }

    private int getCurrentVolume() {
        return this.audioMngr.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private int getMaxVolume() {
        return this.audioMngr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public interface SoundButtonListener {
        void onButtonPressed();
    }
}
