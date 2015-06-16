package net.fdloch.wifiPresenter.android.dialog;

/**
 * Created by florian on 16.06.15.
 */
public class CustomConnectionDialog extends SimpleInputDialog {

    @Override
    protected String getTitle() {
        return "Custom connection";
    }

    @Override
    protected String getMessage() {
        return "Please enter the IP of your server";
    }

}
