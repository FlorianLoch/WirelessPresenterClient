package net.fdloch.wifiPresenter.android.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * Created by florian on 16.06.15.
 */
public abstract class SimpleInputDialog {

    protected abstract String getTitle();

    protected abstract String getMessage();

    protected String getDefaultInputValue() {
        return "";
    }

    public void show(Context context, final DialogListener listener) {
        final EditText eT_input = new EditText(context);
        eT_input.setText(getDefaultInputValue());

        new AlertDialog.Builder(context)
                .setTitle(getTitle())
                .setMessage(getMessage())
                .setView(eT_input)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = eT_input.getText().toString();
                        listener.onFinished(input);
                    }

                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        listener.onCancelled();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }

    public abstract static class DialogListener {
        abstract public void onFinished(String input);

        void onCancelled() {
            //
        }
    }

}
