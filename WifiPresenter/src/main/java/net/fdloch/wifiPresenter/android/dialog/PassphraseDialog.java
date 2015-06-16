package net.fdloch.wifiPresenter.android.dialog;

/**
 * Created by florian on 16.06.15.
 */
public class PassphraseDialog extends SimpleInputDialog {
    @Override
    protected String getTitle() {
        return "Passphrase";
    }

    @Override
    protected String getMessage() {
        return "Please enter the passphrase for the selected server";
    }

    @Override
    protected String getDefaultInputValue() {
        return "TOP_SECRET";
    }
}
