package name.marinchenko.lorryvision.activities.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.activities.info.AboutActivity;
import name.marinchenko.lorryvision.activities.info.InstructionActivity;
import name.marinchenko.lorryvision.activities.info.LicenseActivity;
import name.marinchenko.lorryvision.activities.web.FeedbackActivity;
import name.marinchenko.lorryvision.services.NetScanService;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.debug.LoginDialog;
import name.marinchenko.lorryvision.util.dialogs.ConnectDialog;
import name.marinchenko.lorryvision.util.net.Net;
import name.marinchenko.lorryvision.util.net.NetType;
import name.marinchenko.lorryvision.util.net.NetView;
import name.marinchenko.lorryvision.util.net.NetlistAdapter;
import name.marinchenko.lorryvision.util.threading.ToastThread;

import static name.marinchenko.lorryvision.services.ConnectService.ACTION_CONNECT_MANUAL;
import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_NET_SSID;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_SCAN_SINGLE;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_LORRIES_DETECTED;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_SCANS;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_СONNECT_END;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_СONNECT_END_OK;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_СONNECT_START;
import static name.marinchenko.lorryvision.util.dialogs.ConnectDialog.CONNECT_TAG;

public class MainActivity
        extends ToolbarAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   AdapterView.OnItemClickListener,
                   AdapterView.OnItemLongClickListener {

    private NetlistAdapter netlistAdapter;
    private boolean lorriesDetected = false;
    private ConnectDialog connectDialog;

    /*
     * Overridden methods
     */

    /**
     * Activity lifecycle: activity launched
     * @param savedInstanceState saved state of the activity
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.messenger = new Messenger(new MainIncomingHandler(this));
        this.netlistAdapter = Initializer.Main.initNetlist(this);

        Initializer.Main.init(this);
    }

    /**
     * Activity lifecycle: after onStart() or onPause() when user returns to the activity
     */
    @Override
    protected void onResume() {
        closeDrawer(false);

        super.onResume();

        Initializer.Main.initAutoconnectCheckbox(this);
        Initializer.Main.initAutoUpdate(this, this.lorriesDetected);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissConnectDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dismissConnectDialog();
    }

    /**
     * Activity lifecycle: after onStop() when user navigates to the activity
     */
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Activity lifecycle: after onStop() when activity is finishing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) closeDrawer(true);
        else super.onBackPressed();
    }

    /**
     * Creating menu for toolbar
     * @param menu main menu
     * @return if created
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_popup, menu);
        return true;
    }

    /**
     * Processing toolbar item selection
     * @param item selected item
     * @return if done
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_popup_instruction:
                final Intent instructionIntent = new Intent(this, InstructionActivity.class);
                startActivity(instructionIntent);
                return true;

            case R.id.toolbar_popup_about:
                final Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;

            case R.id.toolbar_popup_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Listening netlist events
     * @param adapterView parent view
     * @param view current list element
     * @param i number of the element in the list
     * @param l id of the element
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final NetView net = (NetView) this.netlistAdapter.getItem(i);

        if (net.getType() == NetType.lorryNetwork) {
            final Intent connectingIntent = new Intent(this, NetScanService.class);

            connectingIntent.setAction(ACTION_CONNECT_MANUAL);
            connectingIntent.putExtra(EXTRA_NET_SSID, net.getSsid());
            startService(connectingIntent);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final LoginDialog dialog = new LoginDialog();
        final Bundle bundle = new Bundle();

        final String id = ((Net) adapterView.getItemAtPosition(i)).getSsid();

        bundle.putString(EXTRA_NET_SSID, id);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "login");

        return true;
    }

    /**
     * Processing sidebar item selection
     * @param item selected item
     * @return if done
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            // Group home
            case R.id.drawer_item_home:
                onBackPressed();
                break;

            // Group main
            case R.id.drawer_item_instruction:
                final Intent instructionIntent = new Intent(this, InstructionActivity.class);
                startActivity(instructionIntent);
                break;

            case R.id.drawer_item_about:
                final Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

            case R.id.drawer_item_license:
                final Intent licenseIntent = new Intent(this, LicenseActivity.class);
                startActivity(licenseIntent);
                break;

            case R.id.drawer_item_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            // Group help
            case R.id.drawer_item_website:
                //TODO website
                break;

            case R.id.drawer_item_feedback:
                final Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
                startActivity(feedbackIntent);
                break;
        }
        return true;
    }

    /**
     * Updating resources when configuration changes
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        Initializer.Main.initOnConfigurationChanges(this);
        this.netlistAdapter = Initializer.Main.initNetlist(this);

        final View decorView = getWindow().getDecorView();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );

        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }


    /*
     * Private methods
     */

    private boolean isDrawerOpen() {
        final DrawerLayout drawer = findViewById(R.id.activity_main);
        return drawer.isDrawerOpen(GravityCompat.START);
    }

    private void closeDrawer(final boolean animate) {
        final DrawerLayout drawer = findViewById(R.id.activity_main);
        drawer.closeDrawer(GravityCompat.START, animate);
    }


    /*
     * Own methods
     */

    /**
     * Click update nets button
     * @param view button
     */
    public void onButtonUpdateClick(final View view) {
        requestScanResults();
    }

    public void onCheckboxAutoConnectClick(View view) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean autoConnect = ((CheckBox) view).isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SettingsFragment.PREF_KEY_AUTOCONNECT, autoConnect);
        editor.apply();

        Initializer.initAutoConnect(this);
    }

    private void updateNetlist(final Bundle bundle) {
        this.netlistAdapter.update(this, bundle);
        this.netlistAdapter.notifyDataSetChanged();
    }

    private void toVideoActivity() {
        final Timer timer = new Timer();
        final TimerTask jumpTask = new TimerTask() {
            @Override
            public void run() {
                final Intent videoIntent = new Intent(getApplicationContext(), VideoActivity.class);
                startActivity(videoIntent);
            }
        };
        timer.schedule(jumpTask, VideoActivity.JUMP_DELAY);
    }

    private void dismissConnectDialog() {
        if (this.connectDialog != null) {
            this.connectDialog.dismiss();
            this.connectDialog = null;
        }
    }

    protected static class MainIncomingHandler extends ToolbarAppCompatActivity.IncomingHandler {

        MainIncomingHandler(ToolbarAppCompatActivity activity) { super(activity); }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity mainActivity = (MainActivity) activity;
            switch (msg.what) {
                case MSG_SCANS:
                    mainActivity.updateNetlist(msg.getData());
                    mainActivity.lorriesDetected = (msg.arg1 == MSG_LORRIES_DETECTED);
                    Initializer.Main.initAutoUpdate(mainActivity, mainActivity.lorriesDetected);
                    break;

                case MSG_СONNECT_START:
                    if (mainActivity.connectDialog == null) {
                        mainActivity.connectDialog = new ConnectDialog();
                        mainActivity.connectDialog.setArguments(msg.getData());
                        mainActivity.connectDialog.show(
                                mainActivity.getFragmentManager(),
                                CONNECT_TAG
                        );
                    }
                    break;

                case MSG_СONNECT_END:
                    mainActivity.dismissConnectDialog();
                    if (msg.arg1 == MSG_СONNECT_END_OK) {
                        ToastThread.postToastMessage(
                                mainActivity,
                                mainActivity.getString(R.string.toast_connection_ok),
                                Toast.LENGTH_SHORT
                        );
                        mainActivity.toVideoActivity();
                    }
                    else {
                        ToastThread.postToastMessage(
                                mainActivity,
                                mainActivity.getString(R.string.toast_connection_failed),
                                Toast.LENGTH_SHORT
                        );
                    }
                    break;

                case MSG_RETURN_TO_MAIN:
                    mainActivity.closeDrawer(true);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
