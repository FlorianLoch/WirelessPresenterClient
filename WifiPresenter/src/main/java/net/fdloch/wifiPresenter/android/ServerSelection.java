package net.fdloch.wifiPresenter.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import net.fdloch.wifiPresenter.android.network.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;


public class ServerSelection extends ActionBarActivity {
    private static final Logger log = LoggerFactory.getLogger(ServerSelection.class);
    public static final String PARCEL_KEY_SERVER_ADDRESS = "server_address";
    private static final String PASSPHRASE = "TOP_SECRET";
    private DiscoveredServerAdapter serverListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        this.serverListAdapter = new DiscoveredServerAdapter();

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Scanning network...", "please be patient", true);

        new ServiceDiscovery().discoverServices(new ServiceDiscovery.Callback() {
            @Override
            public synchronized void onServerFound(final ServiceDiscovery.ServerInformation discoveredServer) {
                log.info(String.format("New server discovered %s", discoveredServer));

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        new AlertDialog.Builder(ServerSelection.this)
//                            .setTitle("Server found!")
//                            .setMessage("A server has been found in your network (" + discoveredServer.getHostname() + ") - do you want to connect to it?")
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    ServerSelection.this.txt_ip.setText(discoveredServer.getAddress().getHostAddress());
//                                }
//                            })
//                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();

                serverListAdapter.add(discoveredServer);
            }

            @Override
            public synchronized void onTimeoutReached(List<ServiceDiscovery.ServerInformation> discoveredServers) {
                progressDialog.dismiss();
            }
        }, 8081, (int) 1E4);

        ListView serverList = (ListView) findViewById(R.id.lv_found_server);
        serverList.setAdapter(this.serverListAdapter);
        serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Pressed: " + id);
                //goToControlActivity();
            }
        });
    }

    private void goToControlActivity(String ip, String passcode) {
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
