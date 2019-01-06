package name.marinchenko.lorryvision.util.net;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Net implementation.
 */

public class Net extends NetConfig
                 implements Comparable<Net> {

    public final static int NET_STATE_DETECTED = 0;
    public final static int NET_STATE_CONNECTED = 1;
    public final static int NET_STATE_DETACHED = 2;

    private final NetType type;
    private final long detectTime;

    private final ArrayList<Integer> level = new ArrayList<>();
    private int state = NET_STATE_DETECTED;
    private boolean autoConnect = true;
    private boolean highlighted = false;
    private boolean wasConnected = false;
    private boolean wasDetached = false;
    private int connectMoment = -1;
    private int detachMoment = -1;

    public Net(final String ssid,
               final String bssid,
               final String caps,
               final String password,
               final int signal) {
        super(ssid, bssid, caps, password);
        this.type = NetBuffer.getType(ssid);
        this.level.add(signal);
        this.detectTime = System.currentTimeMillis();
    }

    @Override
    public int compareTo(@NonNull Net net) {
        if (this.type == NetType.wifiNetwork && net.getType() == NetType.wifiNetwork) {
            return this.ssid.compareToIgnoreCase(net.getSsid());

        } else if (this.type == NetType.lorryNetwork && net.getType() == NetType.wifiNetwork) {
            return -1;

        } else if (this.type == NetType.wifiNetwork && net.getType() == NetType.lorryNetwork) {
            return 1;

        } else {
            return Integer.signum(net.getLevel() - this.getLevel()) +
                    2 * Long.signum(this.detectTime - net.getDetectTime());
        }
    }

    public NetType getType() { return this.type; }
    public int getLevel() { return this.level.get(this.level.size() - 1); }
    public int getConnectMoment() { return this.connectMoment; }
    public int getDetachMoment() { return this.detachMoment; }
    public long getDetectTime() { return this.detectTime; }
    public int getState() { return this.state; }
    public boolean wasConnected() { return this.wasConnected; }
    public boolean wasDetached() { return this.wasDetached; }
    public boolean getHighlighted() { return this.highlighted; }
    public boolean getAutoConnect() { return this.autoConnect; }

    public int getLastTimeMeanLevel(final int sec) {
        if (this.level.size() <= sec) return -100;
        else {
            int sum = 0;
            for (int i = this.level.size() - 1; i > this.level.size() - sec - 1; i--) {
                sum += this.level.get(i);
            }
            return sum / sec;
        }
    }

    public void addLevel(final int level) { this.level.add(level); }

    public void connect() {
        this.state = NET_STATE_CONNECTED;
        this.connectMoment = this.level.size();
        this.wasConnected = true;
    }

    public void setHighlighted(final boolean highlight) {
        this.highlighted = highlight;
    }

    public void setAutoConnect(final boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public void detach() {
        this.state = NET_STATE_DETACHED;
        this.detachMoment = this.level.size();
        this.wasDetached = true;
    }
}
