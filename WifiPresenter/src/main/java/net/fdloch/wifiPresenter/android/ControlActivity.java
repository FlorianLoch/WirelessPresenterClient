package net.fdloch.wifiPresenter.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import net.fdloch.wifiPresenter.android.network.Connection;
import net.fdloch.wifiPresenter.android.network.ConnectionListener;
import net.fdloch.wifiPresenter.android.network.HandshakeLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

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

    private CommandProducer cP;
    private SoundButtonObserver soundButtonObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        ServerAddress sA = intent.getParcelableExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS);

/*
        this.soundButtonObserver = new SoundButtonObserver(this, new Handler());
        this.soundButtonObserver.setOnVolumeDownListener(new SoundButtonObserver.SoundButtonListener() {
            @Override
            public void onButtonPressed() {
                cP.fireNextCommand();
            }
        });
        this.soundButtonObserver.setOnVolumeUpListener(new SoundButtonObserver.SoundButtonListener() {
            @Override
            public void onButtonPressed() {
                cP.fireBackCommand();
            }
        });
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this.soundButtonObserver);
*/

        this.connectToServer(sA);
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
