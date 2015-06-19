package net.fdloch.wifiPresenter.android;

import net.fdloch.wifiPresenter.android.network.CommunicationLayer;
import net.fdloch.wifiPresenter.android.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by florian on 15.03.15.
 */
public class CommandProducer {
    private static final String BACK_CMD = "back";
    private static final String NEXT_CMD = "next";

    private static final Logger log = LoggerFactory.getLogger(CommandProducer.class);
    private CommunicationLayer conn;

    public CommandProducer(CommunicationLayer conn) {
        this.conn = conn;
    }

    private void fireCommand(String command) {
        log.debug("Going to send command '" + command);
        this.conn.send("\\" + command);
    }

    public void fireBackCommand() {
        this.fireCommand(BACK_CMD);
    }

    public void fireNextCommand() {
        this.fireCommand(NEXT_CMD);
    }
}
