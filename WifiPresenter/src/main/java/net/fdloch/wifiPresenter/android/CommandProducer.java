package net.fdloch.wifiPresenter.android;

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
    private Connection conn;
    private boolean enabled;

    public CommandProducer(Connection conn, boolean enabled) {
        this.conn = conn;
        this.enabled = enabled;
    }

    private void fireCommand(String command) {
        if (this.enabled) {
            log.debug("Going to send command '" + command );
            this.conn.send("\\" + command);

            return;
        }

        log.debug("Did not send command '" + command + "' because CommandProducer instance is not enabled.");
    }

    public void fireBackCommand() {
        this.fireCommand(BACK_CMD);
    }

    public void fireNextCommand() {
        this.fireCommand(NEXT_CMD);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
