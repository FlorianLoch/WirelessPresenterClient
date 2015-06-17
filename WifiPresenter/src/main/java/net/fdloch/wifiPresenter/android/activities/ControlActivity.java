package net.fdloch.wifiPresenter.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import net.fdloch.wifiPresenter.android.CommandProducer;
import net.fdloch.wifiPresenter.android.R;
import net.fdloch.wifiPresenter.android.SoundButtonObserver;
import net.fdloch.wifiPresenter.android.SoundButtonService;
import net.fdloch.wifiPresenter.android.network.Connection;
import net.fdloch.wifiPresenter.android.network.ConnectionListener;
import net.fdloch.wifiPresenter.android.network.HandshakeLayer;
import net.fdloch.wifiPresenter.android.types.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ControlActivity extends Activity {
    private static final Logger log = LoggerFactory.getLogger(ControlActivity.class);
    public static final int PORT = 8081;

    private Connection conn;
    private ConnectionListener listener = new ConnectionListener() {
        @Override
        public void onMessage(String msg) {
            cP.setEnabled(true);
        }

        @Override
        public void onError(Exception e) {
            log.error("An error regarding server connection occured!", e);
        }

        @Override
        public void onDisconnect() {
            log.info(String.format("Connection to server '%s' lost!", conn.getRemoteIP()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ControlActivity.this, "Connection to server lost...", Toast.LENGTH_LONG);
                    ControlActivity.this.finish();
                }
            });
        }
    };
    private TextView tV_timer;
    private long startedAt = 0;

    private CommandProducer cP;

    @Override
    public void onBackPressed() {
        System.out.println("Back pressed!");
        try {
            log.info("Going to close connection...");
            this.conn.close();
        } catch (Exception e) {
            log.error("Error while closing connection!", e);
        }
        log.info("Finished ControlActivity!");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        ServerAddress sA = intent.getParcelableExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS);

        this.connectToServer(sA);

        this.tV_timer = (TextView) findViewById(R.id.tv_timer);

        this.startedAt = System.currentTimeMillis();
        setupSchedule();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Intent serviceIntent = new Intent(this, SoundButtonService.class);
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

    private void connectToServer(final ServerAddress address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(address.getHost(), PORT);

                    conn = new Connection(s);
                    new HandshakeLayer(listener, conn, address.getPasscode());

                    conn.start();

                    cP = new CommandProducer(conn, false);
                }
                catch (Exception ex) {
                    listener.onError(new Exception("Could not connect to server!", ex));
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("Called");

        if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            this.cP.fireNextCommand();
            return true;
        }
        else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            this.cP.fireBackCommand();
            return true;
        }
        else if (KeyEvent.KEYCODE_BACK == keyCode) {
            onBackPressed();
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Just suppress Android from playing default sound
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode || KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
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
        this.cP.setEnabled(false);

        try {
            this.conn.close();
        } catch (IOException e) {
            log.warn("Failed to close connection!", e);
            e.printStackTrace();
        }

        log.info("Destroyed activity");

        //getApplicationContext().getContentResolver().unregisterContentObserver(this.soundButtonObserver);
        super.onDestroy();
    }
}
