package net.fdloch.wifiPresenter.android.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by florian on 13.06.15.
 */
public class ServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);
    public static final byte[] DISCOVERY_REQUEST = "BluePresenterServer_Discovery_Request".getBytes();
    public static final String DISCOVERY_RESPONSE = "BluePresenterServer_Discovery_Response";
    private static final String BROADCAST_ADDRESS = "255.255.255.255";

    private boolean stopped = false;

    public void stopDiscovery() {
        this.stopped = true;
        log.info("Stopped service discovery!");
    }

    public void discoverServices(final ServiceDiscovery.Callback onDiscovered, final int port, int timeoutMs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);

                    DatagramPacket requestPacket = new DatagramPacket(DISCOVERY_REQUEST, DISCOVERY_REQUEST.length, InetAddress.getByName(BROADCAST_ADDRESS), port);

                    socket.send(requestPacket);

                    log.info("Send service discovery package via UDP!");

                    byte[] receiveBuffer = new byte[1000]; //It might be UTF-32 in worst case, so 4 bytes per character

                    while (true) {
                        DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(responsePacket);

                        String content = new String(responsePacket.getData()).trim();

                        if (stopped) {
                            break;
                        }

                        log.info(String.format("Received packet from %s:%d", responsePacket.getAddress(), responsePacket.getPort()));
                        log.info(String.format("Contains: '%s' (Length: %d)", content, content.length()));

                        if (content.startsWith(DISCOVERY_RESPONSE)) {
                            onDiscovered.doAction(responsePacket.getAddress(), content.substring(DISCOVERY_RESPONSE.length()));
                        }
                    }
                } catch(IOException e) {
                    log.error("ServiceDiscovery failed due to an exception.", e);
                }
            }
        }).start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ServiceDiscovery.this.stopDiscovery();
            }
        }, timeoutMs);
    }

    public interface Callback {
        void doAction(InetAddress discoveredServer, String hostname);
    }

}
