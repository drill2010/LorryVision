package name.marinchenko.lorryvision.util.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.main.SettingsFragment;


/**
 * ScanResult parsing class to produce Net object.
 */

public class NetBuffer {

    private final static Pattern LORRY = Pattern.compile("LV-[0-9]{8}");


    private String connectingNetSsidAuto;
    private String connectingNetSsidManual;

    private String connectedNetSsid;

    private List<Net> nets = new ArrayList<>();
    private List<Net> lorries = new ArrayList<>();
    private List<Net> previousLorries = new ArrayList<>();

    public NetBuffer() {}

    public List<Net> getNets(final List<ScanResult> results,
                             final Context context) {
        updateAndRemoveNets(results);
        addNets(results, context);

        Collections.sort(this.nets);

        this.previousLorries = this.lorries;
        this.lorries = findLorries();

        return this.nets;
    }

    public boolean lorriesNear() { return this.lorries.size() > 0; }

    public boolean lorriesChanged() {
        if (this.lorries.size() == 0) return false;
        if (this.lorries.size() != this.previousLorries.size()) return true;
        for (int i = 0; i < this.lorries.size(); i++) {
            if (!this.lorries.get(i).getSsid().equals(this.previousLorries.get(i).getSsid())) {
                return true;
            }
        }
        return false;
    }

    public void connect(final boolean autoConnect) {
        for (Net net : this.lorries) {
            if (net.getSsid().equals(
                    autoConnect ? this.connectingNetSsidAuto : this.connectingNetSsidManual
            )) {
                net.connect();
                this.connectingNetSsidAuto = null;
                this.connectingNetSsidManual = null;
                this.connectedNetSsid = net.getSsid();
                return;
            }
        }
    }

    public void detach() {  
        for (Net net : this.lorries) {
           //TODO сделать так, чтобы сети не добавлялись заново в массив lorries при переприсоединении. Тогда функция детач будет работать как задумана.
           // TODO либо сделать поиск по массиву PreviousLorries?
          //  if (net.getSsid().equals(this.connectedNetSsid) && net.wasConnected()) {
            if (net.getSsid().equals(this.connectedNetSsid) ) {
                net.detach();
                net.setAutoConnect(false);
                this.connectingNetSsidAuto = null;
                this.connectingNetSsidManual = null;
                this.connectedNetSsid = null;
                return;
            }
        }
    }

    public void connectFailed() {
        this.connectingNetSsidAuto = null;
        this.connectingNetSsidManual = null;
        this.connectedNetSsid = null;
    }

    @Nullable
    public Net getConnectingNet(final boolean manual) {
        for (Net net : this.lorries) {
            if (manual && net.getSsid().equals(this.connectingNetSsidManual)) {
                return net;
            }
            else if (net.getSsid().equals(this.connectingNetSsidAuto)) {
                return net;
            }
        }
        return null;
    }

    public String getConnectingNetSsid(final boolean manual) {
        final Net net = getConnectingNet(manual);
        return net == null ? "" : net.getSsid();
    }

    @Nullable
    public Net getConnectedNet() {
        for (Net net : this.lorries) {
            if (net.getSsid().equals(this.connectedNetSsid)) {
                return net;
            }
        }
        return null;
    }

    public String getConnectedNetSsid() {
        final Net net = getConnectedNet();
        return net == null ? "" : net.getSsid();
    }

    public void setConnectingNet(@Nullable final String ssid,
                                 final boolean auto) {
        if (auto) this.connectingNetSsidAuto = ssid;
        else {
            this.connectingNetSsidAuto = null;
            this.connectingNetSsidManual = ssid;
        }
    }

    public String getPrefferedNetSsid() {
        for (Net net : lorries) {
            if (net.getAutoConnect() && !net.wasConnected()) {
                return net.getSsid();
            }
        }
        return "";
    }


    public static String getPassword(final Context context,
                                     final String ssid) {
        //TODO password generator for lorry networks
        //return NetStore.getPassword(context, ssid);
       // return "1301900703";
       // return "Getnetwork";

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getString(SettingsFragment.PREF_KEY_LV_PSW, context.getString(R.string.pref_psw_default_value));

    }

    public static NetType getType(final String name){
        Matcher matcher = LORRY.matcher(name);
        return matcher.matches() ? NetType.lorryNetwork : NetType.wifiNetwork;
       // final String pat = "MANSARDA";
       // final String pat = "Temp_WIFI";

        //return name.equals(pat) ? NetType.lorryNetwork : NetType.wifiNetwork;
    }

    private void updateAndRemoveNets(final List<ScanResult> results) {
        out : for (int i = 0; i < this.nets.size(); i++) {
            final Net net = this.nets.get(i);
            for (ScanResult s : results) {
                if (net.getSsid().equals(s.SSID)) {
                    net.addLevel(s.level);
                    continue out;
                }
            }
            this.nets.remove(i);
        }
    }

    private void addNets(final List<ScanResult> results,
                         final Context context) {
        out : for (Iterator<ScanResult> itScan = results.iterator(); itScan.hasNext();) {
            ScanResult s = itScan.next();

            for (Iterator<Net> itNet = this.nets.iterator(); itNet.hasNext();) {
                Net net = itNet.next();
                if (net.getSsid().equals(s.SSID)) continue out;
            }
            if (!s.SSID.equals("")) {
                final Net net = new Net(
                        s.SSID,
                        s.BSSID,
                        s.capabilities,
                        getPassword(context, s.SSID),
                        s.level
                );
                this.nets.add(net);
            }
        }
    }

    private List<Net> findLorries() {
        final List<Net> lorries = new ArrayList<>();
        for (int i = 0; i < this.nets.size(); i++) {
            if (this.nets.get(i).getType() == NetType.lorryNetwork) {
                this.nets.get(i).setHighlighted(i == 0);
                lorries.add(this.nets.get(i));
            }
        }
        return lorries;
    }
}
