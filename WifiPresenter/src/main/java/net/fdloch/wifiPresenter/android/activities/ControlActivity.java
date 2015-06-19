package net.fdloch.wifiPresenter.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import net.fdloch.wifiPresenter.android.ObserverService;
import net.fdloch.wifiPresenter.android.R;
import net.fdloch.wifiPresenter.android.types.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class ControlActivity extends Activity {
    private static final Logger log = LoggerFactory.getLogger(ControlActivity.class);
    private TextView tV_timer;
    private long startedAt = 0;
    private AudioManager audioManager;

//    @Override
//    public void onBackPressed() {
//        System.out.println("Back pressed!");
//        try {
//            log.info("Going to close connection...");
//            this.conn.close();
//        } catch (Exception e) {
//            log.error("Error while closing connection!", e);
//        }
//        log.info("Finished ControlActivity!");
//        finish();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        this.tV_timer = (TextView) findViewById(R.id.tv_timer);

        this.startedAt = System.currentTimeMillis();
        setupSchedule();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        ServerAddress serverAddress = getIntent().getParcelableExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS);

        Intent serviceIntent = new Intent(this, ObserverService.class);
        serviceIntent.putExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS, serverAddress);
        startService(serviceIntent);
    }

    private void setupSchedule() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTimer();
                    }
                });

                setupSchedule();
            }
        }, 1000);
    }

    private void updateTimer() {
        int a = Math.round((System.currentTimeMillis() - startedAt) / 1000.0f);
        int hours = a / 3600;
        a -= hours * 3600;
        int minutes = a / 60;
        a -= minutes * 60;
        int seconds = a;

        String str = "";
        if (hours > 0) str += String.format("%d h : ", hours);
        if (minutes > 0 || hours > 0) str += String.format("%d m : ", minutes);
        str += String.format("%d s", seconds);

        tV_timer.setText(str);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //This could be replaced when using IPC

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            this.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            this.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        log.info("Destroying activity...");

        Intent serviceIntent = new Intent(this, ObserverService.class);
        stopService(serviceIntent);

        log.info("Destroyed activity");

        super.onDestroy();
    }
}
