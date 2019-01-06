package name.marinchenko.lorryvision.util.net;

import android.net.wifi.WifiConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valentin on 09.03.2018.
 */

public class NetConfig {

    protected final String ssid;
    protected final String bssid;
    protected final String caps;
    protected final String password;

    public NetConfig(final String ssid,
                     final String bssid,
                     final String caps,
                     final String password) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.caps = caps;
        this.password = password;
    }

    public NetConfig(final List<String> list) {
        this.ssid = list.get(0);
        this.bssid = list.get(1);
        this.caps = list.get(2);
        this.password = list.get(3);
    }

    public ArrayList<String> asArrayList() {
        final ArrayList<String> list = new ArrayList<>();
        list.add(this.ssid);
        list.add(this.bssid);
        list.add(this.caps);
        list.add(this.password);
        return list;
    }

    public WifiConfiguration getWifiConfiguration() {
        return WifiConfig.configure(this);
    }

    public final String getSsid() { return this.ssid; }
    public final String getBssid() { return this.bssid; }
    public final String getCaps() { return this.caps; }
    public final String getPassword() { return this.password; }

}
