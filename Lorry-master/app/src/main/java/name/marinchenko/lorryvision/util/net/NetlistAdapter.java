package name.marinchenko.lorryvision.util.net;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import name.marinchenko.lorryvision.R;

import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_DISPLAY_GENERAL;


public class NetlistAdapter extends BaseAdapter {

    private final static int ID_MAX_LENGTH = 21;
    private final static int[] SIGNAL_LIST = new int[]{
            R.drawable.ic_wifi_0,
            R.drawable.ic_wifi_1,
            R.drawable.ic_wifi_2,
            R.drawable.ic_wifi_3,
            R.drawable.ic_wifi_4
    };

    private final ArrayList<NetView> netViews = new ArrayList<>();
    private final LayoutInflater inflater;

    public NetlistAdapter(final Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.netViews.size();
    }

    @Override
    public Object getItem(int i) { return this.netViews.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(final int i,
                        final View convertView,
                        final ViewGroup viewGroup) {
        final View view =
                convertView == null
                        ? inflater.inflate(R.layout.item_netlist, viewGroup, false)
                        : convertView;

        final NetView net = this.netViews.get(i);

        final String netId = net.getSsid();
        final NetType type = net.getType();
        final boolean highlight = net.getHighlighted();
        final boolean wasDetached = net.wasDetached();

        view.findViewById(R.id.netList_item_background).setVisibility(
                highlight ? View.VISIBLE : View.GONE
        );

        view.findViewById(R.id.netList_imageView_was_detached).setVisibility(
                type == NetType.lorryNetwork && wasDetached ? View.VISIBLE : View.GONE
        );

        ((ImageView) view.findViewById(R.id.netList_imageView_typeOfNet)).setImageResource(
                type == NetType.lorryNetwork
                        ? R.drawable.ic_type_lorry
                        : R.drawable.ic_type_wifi
        );

        TextView textView = view.findViewById(R.id.netList_textView_netId);
        textView.setText(netId);
        textView.setSelected(netId.length() > ID_MAX_LENGTH);

        ((ImageView) view.findViewById(R.id.netList_imageView_signal))
                .setImageResource(SIGNAL_LIST[net.getSignalIcon()]);

        return view;
    }

    public void update(final Activity activity,
                       final Bundle bundle) {
        this.netViews.clear();
        final List<NetView> newNetViews = NetView.getNetViewList(bundle);

        final SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity);

        if (!sharedPref.getBoolean(PREF_KEY_DISPLAY_GENERAL, false)) {
            for (NetView net : newNetViews) {
                if (net.getType() == NetType.lorryNetwork) this.netViews.add(net);
            }
        } else {
            this.netViews.addAll(newNetViews);
        }
    }
}
