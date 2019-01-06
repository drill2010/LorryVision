package name.marinchenko.lorryvision.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import name.marinchenko.lorryvision.services.NetScanService;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.net.WifiAgent;
import name.marinchenko.lorryvision.util.threading.ToastThread;

import static android.content.Intent.ACTION_USER_PRESENT;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_SCAN_SINGLE;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;

public abstract class ToolbarAppCompatActivity extends AppCompatActivity {

    private BroadcastReceiver wifiReceiver = new WifiAgent.WifiReceiver();
    protected Messenger messenger = new Messenger(new IncomingHandler(this));


    public void initToolbar(@IdRes final int toolbarId,
                            @StringRes final int titleId,
                            final boolean up) {
        final Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setTitle(titleId);
        ab.setDisplayHomeAsUpEnabled(up);
        ab.setDisplayShowHomeEnabled(up);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Initializer.initActivityMessenger(this, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiAgent.enableWifi(this, false, true);
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
        Initializer.initActivityMessenger(this, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Hide keyboard if focus is out of any EditText
     * @param event touch
     * @return super
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public Messenger getMessenger() { return this.messenger; }

    private void registerReceivers() {
        final IntentFilter wifiFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(this.wifiReceiver, wifiFilter);
    }

    private void unregisterReceivers() {
        try {
            unregisterReceiver(this.wifiReceiver);
        } catch (IllegalArgumentException e) {
            Log.w("MyLog", e.getMessage(), e);
        }
    }

    protected void requestScanResults() {
        final Intent scanRequest = new Intent(this, NetScanService.class);
        scanRequest.setAction(ACTION_SCAN_SINGLE);
        startService(scanRequest);
    }


    protected static class IncomingHandler extends android.os.Handler {
        protected final ToolbarAppCompatActivity activity;

        public IncomingHandler(final ToolbarAppCompatActivity activity) {
            this.activity = new WeakReference<>(activity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RETURN_TO_MAIN:
                    this.activity.onBackPressed();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
