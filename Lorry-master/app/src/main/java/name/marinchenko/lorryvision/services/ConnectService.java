package name.marinchenko.lorryvision.services;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import name.marinchenko.lorryvision.util.net.NetConfig;
import name.marinchenko.lorryvision.util.net.WifiAgent;
import name.marinchenko.lorryvision.util.net.WifiConfig;
import name.marinchenko.lorryvision.util.threading.DefaultExecutorSupplier;

/**
 * Service providing connection to the network.
 */

public class ConnectService extends Service {

    public final static String ACTION_CONNECT_MANUAL = "action_connect_manual";
    public final static String ACTION_CONNECT_AUTO = "action_connect_auto";
    public final static String ACTION_CONNECT_FAILED = "action_connect_failed";
    public final static String ACTION_DISCONNECT = "action_disconnect";

    public final static String ACTION_WIFIAGENT_CONNECT = "action_wifiagent_connect";
    public final static String ACTION_WIFIAGENT_CONNECTED_TO = "action_wifiagent_connected_to";
    public final static String ACTION_WIFIAGENT_CONNECTED = "action_wifiagent_connected";
    public final static String ACTION_WIFIAGENT_DISCONNECT = "action_wifiagent_disconnect";
    public final static String ACTION_WIFIAGENT_CONNECT_START = "action_wifiagent_connect_start";

    public final static String EXTRA_NET_CONFIG = "extra_net_config";
    public final static String EXTRA_NET_SSID = "extra_net_ssid";
    public final static String EXTRA_CONNECT_AUTO = "extra_connect_auto";

    public final static int STABLE_CONNECT_TIME_S = 5;
    public final static int STABLE_DETECT_TIME_S = 2;
    public final static int STABLE_LEVEL_DB = -80;

    private final static int CNT_CHECK_CONNECTED = 20;
    private final static int PERIOD_CHECK_CONNECTED_MS = 500;

    private boolean connecting = false;
    private int currentConnectedNetId = -1;
    private WifiConfiguration lastConfig;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) process(intent);
        return START_STICKY;
    }

    private void process(@NonNull final Intent intent) {
        final String action = intent.getAction() == null ? "" : intent.getAction();
        switch (action) {
            case ACTION_WIFIAGENT_CONNECT:
                connect(intent.getStringArrayListExtra(EXTRA_NET_CONFIG));
                break;

            case ACTION_WIFIAGENT_CONNECTED:
                this.connecting = false;
                break;

            case ACTION_WIFIAGENT_DISCONNECT:
                this.connecting = false;
                break;

            case ACTION_DISCONNECT:
                disconnect(ACTION_DISCONNECT);
                break;
        }
    }

    private void connect(final ArrayList<String> configList) {
        final NetConfig netConfig = new NetConfig(configList);
        if (!this.connecting && configList != null && !WifiAgent.connectedTo(
                getApplicationContext(),
                WifiConfig.formatSsid(netConfig.getSsid()))) {

            notifyConnectStart(netConfig.getSsid());

            this.connecting = true;
            this.lastConfig = netConfig.getWifiConfiguration();

            this.currentConnectedNetId = WifiAgent.connect(
                    getApplicationContext(),
                    lastConfig
            );

            final Timer timer = new Timer();
            final TimerTask checkConnectionTask = new CheckConnectionTask(netConfig.getSsid());
            timer.schedule(checkConnectionTask, 0, PERIOD_CHECK_CONNECTED_MS);
        }
    }

    private void notifyConnectStart(final String ssid) {
        if (!this.connecting) {
            final Intent start = new Intent(this, NetScanService.class);
            start.setAction(ACTION_WIFIAGENT_CONNECT_START);
            start.putExtra(EXTRA_NET_SSID, ssid);
            startService(start);
        }
    }

    private void disconnect(final String action) {
        WifiAgent.disconnect(this, this.currentConnectedNetId);
        sendConnectionState(action);
        this.connecting = false;
        this.currentConnectedNetId = -1;
        stopSelf();
    }

    private void sendConnectionState(final String action) {
        final Intent state = new Intent(this, NetScanService.class);
        state.setAction(action);
        startService(state);
    }


    private class CheckConnectionTask extends TimerTask {
        private final String ssid;
        private int cnt = 0;

        CheckConnectionTask(final String ssid) {
            this.ssid = ssid;
        }

        @Override
        public void run() {
            if (cnt < CNT_CHECK_CONNECTED) {
                if (WifiAgent.connectedTo(getApplicationContext(), WifiConfig.formatSsid(ssid))) {
                    sendConnectionState(ACTION_WIFIAGENT_CONNECTED_TO);
                    this.cancel();
                    cnt += CNT_CHECK_CONNECTED;
                }
            }
            else {
                sendConnectionState(ACTION_CONNECT_FAILED);
                this.cancel();
            }

            cnt++;
        }
    }
}
