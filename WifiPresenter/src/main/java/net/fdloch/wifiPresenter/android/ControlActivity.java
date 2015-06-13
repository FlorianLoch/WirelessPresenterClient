package net.fdloch.wifiPresenter.android;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import net.fdloch.wifiPresenter.android.network.Connection;
import net.fdloch.wifiPresenter.android.network.ConnectionListener;
import net.fdloch.wifiPresenter.android.network.HandshakeLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;


public class ControlActivity extends ActionBarActivity implements ConnectionListener {
    private static final Logger log = LoggerFactory.getLogger(ControlActivity.class);
    public static final int PORT = 8081;

    private Connection conn;
    private CommandProducer cP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        ServerAddress sA = intent.getParcelableExtra(ServerSelection.PARCEL_KEY_SERVER_ADDRESS);

        this.connectToServer(sA);
    }

    private void connectToServer(final ServerAddress address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(address.getHost(), PORT);

                    conn = new Connection(s);
                    new HandshakeLayer(ControlActivity.this, conn, address.getPasscode());

                    conn.start();

                    cP = new CommandProducer(conn, false);
                }
                catch (Exception ex) {
                    ControlActivity.this.onError(new Exception("Could not connect to server!", ex));
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            this.cP.fireNextCommand();
        }
        else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            this.cP.fireBackCommand();
        }

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Just suppress Android from playing default sound
        return true;
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
    public void onMessage(String msg) {
        this.cP.setEnabled(true);
    }

    @Override
    public void onError(Exception e) {
        log.error("Exception caught: ", e);
        e.printStackTrace();
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    protected void onDestroy() {
        this.cP.setEnabled(false);

        try {
            this.conn.close();
        } catch (IOException e) {
            log.warn("Failed to close connection!", e);
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
