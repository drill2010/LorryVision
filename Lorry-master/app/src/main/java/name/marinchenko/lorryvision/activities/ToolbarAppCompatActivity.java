package name.marinchenko.lorryvision.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.services.NetScanService;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.net.WifiAgent;

import static name.marinchenko.lorryvision.services.NetScanService.ACTION_SCAN_SINGLE;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;

public abstract class ToolbarAppCompatActivity extends AppCompatActivity
                implements ActivityCompat.OnRequestPermissionsResultCallback {

    private BroadcastReceiver wifiReceiver = new WifiAgent.WifiReceiver();
    protected Messenger messenger = new Messenger(new IncomingHandler(this));


    private static final int PERMISSION_REQUEST_LOCATION = 0;
    Activity activity;
    Toolbar toolbar;
    private AlertDialog alertDialog;

    public void initToolbar(
                            Activity currentActivity,
                            @IdRes final int toolbarId,
                            @StringRes final int titleId,
                            final boolean up) {
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        activity = currentActivity;
        final ActionBar ab = getSupportActionBar();
        ab.setTitle(titleId);
        ab.setDisplayHomeAsUpEnabled(up);
        ab.setDisplayShowHomeEnabled(up);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(toolbar, R.string.toast_location_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                startWifi();
            } else {
                // Permission request was denied.
                Snackbar.make(toolbar, R.string.toast_location_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        } else {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
    }

    void startWifi(){
        // TODO Add Check presence of location feature

        Log.d("test_toolbar", toolbar.getTitle().toString());
        if (!isGpsEnabled(getApplicationContext()))
            showAlertDialog(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        WifiAgent.enableWifi(this, false, true);
        registerReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Initializer.initActivityMessenger(this, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
       Log.d("test_Activity", "Onstart appCompat");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            Snackbar.make(toolbar,
                    R.string.toast_location_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            startWifi();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }


    public void showAlertDialog(String event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(event.equals(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)){
            builder.setTitle("Location services are off");
            builder.setMessage("Please turn on location services to continue");
            builder.setCancelable(false);
            builder.setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO
                }
            });
            builder.setPositiveButton("Turn on location", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void showPermissionAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


            builder.setTitle(R.string.toast_location_permission_unavailable);
            builder.setMessage(R.string.toast_location_permission_need);
            builder.setCancelable(false);
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO
                }
            });
            builder.setPositiveButton("Proceed to permission", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            });
            alertDialog = builder.create();
            alertDialog.show();

    }


    private void requestLocationPermission() {
        showPermissionAlertDialog();
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
