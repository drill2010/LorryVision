package name.marinchenko.lorryvision.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.util.net.WifiAgent;

import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_NET_SSID;


/**
 * Connect dialog for connection cancelling.
 */

public class ConnectDialog
        extends DialogFragment
        implements DialogInterface.OnClickListener {

    public static final String CONNECT_TAG = "connect_tag";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View form = getActivity()
                .getLayoutInflater()
                .inflate(R.layout.connect_dialog, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(form);
        builder.setTitle(
                getString(R.string.connect_dialog_title) + " " + getArguments().getString(EXTRA_NET_SSID)
        );
        builder.setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        WifiAgent.notifyDisconnectedManual(getActivity());
    }

    @Override
    public void onDismiss(DialogInterface unused) { super.onDismiss(unused); }

    @Override
    public void onCancel(DialogInterface unused) { super.onCancel(unused); }
}
