package net.fdloch.wifiPresenter.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import net.fdloch.wifiPresenter.android.network.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


public class ServerSelection extends ActionBarActivity {
    private static final Logger log = LoggerFactory.getLogger(ServerSelection.class);
    public static final String PARCEL_KEY_SERVER_ADDRESS = "server_address";
    private static final String PASSPHRASE = "TOP_SECRET";
    public Button btn_connect;
    public EditText txt_ip;
    public EditText txt_passphrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        this.btn_connect = (Button) findViewById(R.id.btn_connect);
        this.txt_ip = (EditText) findViewById(R.id.txt_ip);
        this.txt_passphrase = (EditText) findViewById(R.id.txt_passphrase);

        this.txt_passphrase.setText(PASSPHRASE);

        this.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToControlActivity();
            }
        });

        new ServiceDiscovery().discoverServices(new ServiceDiscovery.Callback() {
            @Override
            public void doAction(final InetAddress discoveredServer, final String hostname) {
                log.info(String.format("New server discovered %s", discoveredServer));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(ServerSelection.this)
                            .setTitle("Server found!")
                            .setMessage("A server has been found in your network (" + hostname + ") - do you want to connect to it?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ServerSelection.this.txt_ip.setText(discoveredServer.getHostAddress());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    }
                });
            }
        }, 8081, (int) 1E4);
    }

    private void goToControlActivity() {
        String ip = this.txt_ip.getText().toString();
        String passcode = this.txt_passphrase.getText().toString();

        ServerAddress serverAddress = new ServerAddress(ip, passcode);

        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(PARCEL_KEY_SERVER_ADDRESS, serverAddress);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onConnectButtonClick() {

    }
}
