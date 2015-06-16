package net.fdloch.wifiPresenter.android;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

/**
 * Created by florian on 14.06.15.
 */
public class SoundButtonObserver extends ContentObserver {
    private int previousVolume;
    private AudioManager audioMngr;
    private SoundButtonListener volumeUpListener;
    private SoundButtonListener volumeDownListener;

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
    }

    public void setOnVolumeUpListener(SoundButtonListener listener) {
        this.volumeUpListener = listener;
    }

    public void setOnVolumeDownListener(SoundButtonListener listener) {
        this.volumeDownListener = listener;
    }


    private int getCurrentVolume() {
        return this.audioMngr.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        int delta = this.previousVolume - getCurrentVolume();
        this.previousVolume = getCurrentVolume();

        if (delta < 0) {
            this.volumeUpListener.onButtonPressed();
        }
        else if (delta > 0) {
            this.volumeDownListener.onButtonPressed();
        }
    }

    public interface SoundButtonListener {
        void onButtonPressed();
    }
}
