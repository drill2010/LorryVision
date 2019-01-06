package name.marinchenko.lorryvision.util.debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import name.marinchenko.lorryvision.R;

import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_NET_SSID;


/**
 * Dialog window for saving network ID-Passwords pairs.
 */
public class LoginDialog
        extends DialogFragment
        implements DialogInterface.OnClickListener {

    private View form = null;
    private String id = "";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.form = getActivity().getLayoutInflater().inflate(R.layout.password_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        this.id = getArguments().getString(EXTRA_NET_SSID);

        builder.setView(this.form);
        builder.setTitle(this.id);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.setNeutralButton("УДАЛИТЬ ВСЕ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NetStore.deleteAll(getActivity());
                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        ((EditText) this.form.findViewById(R.id.edittext_password))
                .setText(NetStore.getPassword(getActivity(), this.id));

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final String password = ((EditText) this.form
                .findViewById(R.id.edittext_password))
                .getText()
                .toString();

        NetStore.save(getActivity(), this.id, password);
    }

    @Override
    public void onDismiss(DialogInterface unused) { super.onDismiss(unused); }

    @Override
    public void onCancel(DialogInterface unused) { super.onCancel(unused); }
}