package name.marinchenko.lorryvision.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.Notificator;
import name.marinchenko.lorryvision.util.net.Net;
import name.marinchenko.lorryvision.util.net.NetBuffer;
import name.marinchenko.lorryvision.util.net.NetConfig;
import name.marinchenko.lorryvision.util.net.NetView;
import name.marinchenko.lorryvision.util.net.WifiAgent;
import name.marinchenko.lorryvision.util.net.WifiConfig;
import name.marinchenko.lorryvision.util.threading.DefaultExecutorSupplier;

import static name.marinchenko.lorryvision.services.ConnectService.ACTION_CONNECT_AUTO;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_CONNECT_FAILED;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_CONNECT_MANUAL;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_DISCONNECT;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_WIFIAGENT_CONNECTED_TO;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_WIFIAGENT_CONNECT_START;
import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_CONNECT_AUTO;
import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_NET_SSID;
import static name.marinchenko.lorryvision.services.ConnectService.STABLE_LEVEL_DB;
import static name.marinchenko.lorryvision.services.ConnectService.STABLE_CONNECT_TIME_S;


/**
 * Service for scanning Wi-Fi networks.
 */

public class NetScanService extends Service {

    public final static int MSG_СONNECT_START = 0;
    public final static int MSG_СONNECT_END = 10;
    public final static int MSG_СONNECT_END_FAILED = 11;
    public final static int MSG_СONNECT_END_OK = 12;
    public final static int MSG_DISCONNECTED = 2;
    public final static int MSG_LORRIES_DETECTED = 3;
    public final static int MSG_RETURN_TO_MAIN = 4;
    public final static int MSG_SCANS = 5;

    public final static String MESSENGER = "messenger_main_activity";

    public final static String ACTION_SCAN_SINGLE = "action_scan_single";
    public final static String ACTION_SCAN_START = "action_scan_start";
    public final static String ACTION_SCAN_STOP = "action_scan_stop";
    public final static String ACTION_UNREGISTER_MESSENGER = "action_unregister_messenger";
    public final static String ACTION_REGISTER_MESSENGER = "action_register_messenger";

    private final static int SCAN_PERIOD_MS = 1000;

    private Timer scanTimer;
    private NetBuffer netBuffer = new NetBuffer();

    private Messenger mActivityMessenger;
    private WifiAgent wifiAgent;

    private boolean scanning = false;
    private boolean autoConnect = false;
    private boolean lorriesNear = false;
    private boolean manualConnectActivated = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() { this.wifiAgent = new WifiAgent(this); }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (intent != null) process(intent);
            }
        });

        return START_STICKY;
    }

    private void process(@NonNull final Intent intent) {
        switch (intent.getAction() == null ? "" : intent.getAction()) {
            case ACTION_SCAN_SINGLE:
                updateScanResults();
                break;

            case ACTION_SCAN_START:
                startScan(0);
                break;

            case ACTION_SCAN_STOP:
                stopScan();
                break;

            case ACTION_CONNECT_AUTO:
                this.autoConnect = intent.getBooleanExtra(EXTRA_CONNECT_AUTO, true);
                if (!this.autoConnect) this.netBuffer.setConnectingNet(null, true);
                break;

            case ACTION_CONNECT_MANUAL:
                this.manualConnectActivated = true;
                this.netBuffer.setConnectingNet(intent.getStringExtra(EXTRA_NET_SSID), false);
                break;

            case ACTION_CONNECT_FAILED:
                this.netBuffer.connectFailed();
                sendMsgСonnectEnd(MSG_СONNECT_END_FAILED);
                break;

            case ACTION_WIFIAGENT_CONNECT_START:
                sendMsgConnectStart(intent.getStringExtra(EXTRA_NET_SSID));
                break;

            case ACTION_WIFIAGENT_CONNECTED_TO:
                this.netBuffer.connect(this.autoConnect);
                sendMsgСonnectEnd(MSG_СONNECT_END_OK);
                break;

            case ACTION_DISCONNECT:
                this.netBuffer.detach();
                break;

            case ACTION_REGISTER_MESSENGER:
                final Messenger activityMessenger = intent.getParcelableExtra(MESSENGER);
                if (activityMessenger != null) this.mActivityMessenger = activityMessenger;
                break;

            case ACTION_UNREGISTER_MESSENGER:
                this.mActivityMessenger = null;
                break;
        }
    }

    @Override
    public void onDestroy() {
        stopScan();
        super.onDestroy();
    }

    public boolean isMessengerNull() {
        return this.mActivityMessenger == null;
    }

    public void sendMessage(final Message msg) {
        if (this.mActivityMessenger != null) {
            try {
                this.mActivityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMsgConnectStart(final String ssid) {
        Notificator.jumpToMainActivity(this);

        final Message msg = new Message();
        msg.what = MSG_СONNECT_START;
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_NET_SSID, ssid);

        msg.setData(bundle);
        sendMessage(msg);
    }

    private void sendMsgСonnectEnd(final int result) {
        final Message msg = new Message();
        msg.what = MSG_СONNECT_END;
        msg.arg1 = result;
        sendMessage(msg);
    }

    private void sendMsgScanResults(final List<Net> nets) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final Message msg = new Message();
                msg.what = MSG_SCANS;
                msg.arg1 = lorriesNear ? MSG_LORRIES_DETECTED : -1;
                msg.setData(NetView.getBundle(nets));

                sendMessage(msg);
            }
        });
    }

    private void sendMsgDisconnected() {
        final Message msg = new Message();
        msg.what = MSG_DISCONNECTED;
        sendMessage(msg);
    }

    private void updateScanResults() {
        final List<Net> nets = this.netBuffer.getNets(wifiAgent.getScanResults(), this);
        sendMsgScanResults(nets);

        if (this.netBuffer.lorriesNear()) {
            if (this.netBuffer.lorriesChanged()) Notificator.notifyNetDetected(this);
            startScan(SCAN_PERIOD_MS);
            lorriesNear();
            this.lorriesNear = true;
        }
        else {
            Initializer.initNetScanService(this);
            Notificator.removeNetDetectedNotification(this);
            this.lorriesNear = false;
        }

        checkConnectedNet();
    }

    private void checkConnectedNet() {
        final Net connected = this.netBuffer.getConnectedNet();

        if (connected != null
                && connected.getLastTimeMeanLevel(STABLE_CONNECT_TIME_S)
                < STABLE_LEVEL_DB) {
            startDisconnectService();
            sendMsgDisconnected();
            Notificator.jumpToMainActivity(this);
        }
    }

    private void startDisconnectService() {
        final Intent disconnect = new Intent(this, ConnectService.class);
        disconnect.setAction(ACTION_DISCONNECT);
        startService(disconnect);
    }

    private void startScan(final int delayMs) {
        if (!this.scanning) {
            stopScan();
            this.scanTimer = new Timer();
            this.scanTimer.schedule(new ScanTimerTask(), delayMs, SCAN_PERIOD_MS);
            this.scanning = true;
        }
    }

    private void stopScan() {
        if (!this.lorriesNear && this.scanTimer != null && this.scanning) {
            this.scanTimer.cancel();
            this.scanTimer = null;
            this.scanning = false;
        }
    }

    private void lorriesNear() {
        if (this.autoConnect) {
            this.netBuffer.setConnectingNet(this.netBuffer.getPrefferedNetSsid(), true);
        }

        final Net net = this.netBuffer.getConnectingNet(
                this.manualConnectActivated
        );

        if (canConnect(net)) connect(net);
    }

    private boolean canConnect(final Net net) {
        return net != null

                && net.getLastTimeMeanLevel(STABLE_CONNECT_TIME_S) > STABLE_LEVEL_DB

                && this.mActivityMessenger != null

                && !Notificator.isLocked(this)

                && Notificator.isScreenOn(this, false)

                && !WifiAgent.connectedTo(this,
                WifiConfig.formatSsid(this.netBuffer.getConnectingNetSsid(
                        this.manualConnectActivated
                )))

                && !WifiAgent.connectedTo(this,
                WifiConfig.formatSsid(this.netBuffer.getConnectedNetSsid()));
    }

    private void connect(@NonNull final Net net) {
        this.manualConnectActivated = false;
        startConnectService(net);
    }

    private void startConnectService(final NetConfig config) {
        final Intent connectService = new Intent(this, ConnectService.class);

        connectService.setAction(ConnectService.ACTION_WIFIAGENT_CONNECT);
        connectService.putStringArrayListExtra(
                ConnectService.EXTRA_NET_CONFIG,
                config.asArrayList()
        );

        startService(connectService);
    }


    private class ScanTimerTask extends TimerTask {
        @Override
        public void run() {
            if (!isMessengerNull()) {
                WifiAgent.enableWifi(getApplicationContext(), false, true);
            }
            updateScanResults();
        }
    }
}
