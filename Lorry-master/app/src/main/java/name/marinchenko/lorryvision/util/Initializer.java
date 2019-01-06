package name.marinchenko.lorryvision.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import name.marinchenko.lorryvision.BuildConfig;
import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.activities.info.AboutActivity;
import name.marinchenko.lorryvision.activities.info.InstructionActivity;
import name.marinchenko.lorryvision.activities.info.LicenseActivity;
import name.marinchenko.lorryvision.activities.main.MainActivity;
import name.marinchenko.lorryvision.activities.main.SettingsActivity;
import name.marinchenko.lorryvision.activities.main.VideoActivity;
import name.marinchenko.lorryvision.activities.web.FeedbackActivity;
import name.marinchenko.lorryvision.services.NetScanService;
import name.marinchenko.lorryvision.util.net.NetlistAdapter;
import name.marinchenko.lorryvision.util.net.WifiAgent;
import name.marinchenko.lorryvision.util.threading.DefaultExecutorSupplier;

import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_AUTOCONNECT;
import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_AUTOUPDATE;
import static name.marinchenko.lorryvision.services.ConnectService.ACTION_CONNECT_AUTO;
import static name.marinchenko.lorryvision.services.ConnectService.EXTRA_CONNECT_AUTO;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_REGISTER_MESSENGER;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_SCAN_START;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_SCAN_STOP;
import static name.marinchenko.lorryvision.services.NetScanService.ACTION_UNREGISTER_MESSENGER;
import static name.marinchenko.lorryvision.services.NetScanService.MESSENGER;


/**
 * Initializer includes static methods for activity initialising.
 */

public class Initializer {

    public static void initNetScanService(final Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final Intent serviceIntent = new Intent(context, NetScanService.class);
                        final boolean auto = isAutoUpdate(context);

                        serviceIntent.setAction(auto ? ACTION_SCAN_START : ACTION_SCAN_STOP);
                        context.startService(serviceIntent);
                    }
                }
        );
    }

    public static void initAutoConnect(final Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final Intent serviceIntent = new Intent(context, NetScanService.class);
                        final boolean auto = isAutoConnect(context);

                        serviceIntent.setAction(ACTION_CONNECT_AUTO);
                        serviceIntent.putExtra(EXTRA_CONNECT_AUTO, auto);
                        context.startService(serviceIntent);
                    }
                }
        );
    }

    public static boolean isAutoConnect(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_AUTOCONNECT, true);
    }

    public static boolean isAutoUpdate(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_AUTOUPDATE, true);
    }

    public static void initActivityMessenger(final ToolbarAppCompatActivity activity,
                                             final boolean start) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final Intent netScanServiceIntent = new Intent(activity, NetScanService.class);

                if (start) {
                    netScanServiceIntent.setAction(ACTION_REGISTER_MESSENGER);
                    netScanServiceIntent.putExtra(MESSENGER, activity.getMessenger());
                }
                else netScanServiceIntent.setAction(ACTION_UNREGISTER_MESSENGER);

                activity.startService(netScanServiceIntent);
            }
        });
    }


    public static class Main {
        public static void init(final MainActivity mainActivity) {
            WifiAgent.enableWifi(mainActivity, true, false);
            mainActivity.initToolbar(
                    R.id.activity_main_toolbar,
                    R.string.app_name,
                    false
            );
            initDrawer(mainActivity);
            initNetScanService(mainActivity);
            initAutoConnect(mainActivity);
        }

        public static void initOnConfigurationChanges(final MainActivity mainActivity) {
            mainActivity.initToolbar(
                    R.id.activity_main_toolbar,
                    R.string.app_name,
                    false
            );
            initDrawer(mainActivity);
        }

        public static void initAutoUpdate(final MainActivity mainActivity,
                                          final boolean lorriesDetected) {
            final Button updateButton = mainActivity.findViewById(R.id.netList_button_updateNets);

            if (lorriesDetected || isAutoUpdate(mainActivity)) {
                updateButton.setEnabled(false);
                updateButton.setText(lorriesDetected
                        ? R.string.netList_button_updateNets_lorries
                        : R.string.netList_button_updateNets_auto
                );

            } else {
                updateButton.setEnabled(true);
                updateButton.setText(R.string.netList_button_updateNets);
            }
        }


        /**
         * Initialising sidebar
         * @param mainActivity MainActivity
         */
        private static void initDrawer(final MainActivity mainActivity) {
            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            final DrawerLayout drawer =
                                    mainActivity.findViewById(R.id.activity_main);
                            final Toolbar toolbar =
                                    mainActivity.findViewById(R.id.activity_main_toolbar);
                            final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                                    mainActivity,
                                    drawer,
                                    toolbar,
                                    R.string.drawer_open,
                                    R.string.drawer_close
                            );

                            drawer.addDrawerListener(toggle);
                            toggle.syncState();

                            final NavigationView navigationView =
                                    mainActivity.findViewById(R.id.drawer_nav);
                            navigationView.setNavigationItemSelectedListener(mainActivity);
                            initVersion(mainActivity, navigationView);
                        }
                    }
            );
        }

        /**
         * Initialising sidebar version textView
         * @param mainActivity MainActivity
         * @param view navigation view of sidebar
         */
        private static void initVersion(final MainActivity mainActivity,
                                        final NavigationView view) {
            TextView version = view
                    .getHeaderView(0)
                    .findViewById(R.id.drawer_textView_version);
            version.setText(String.format(
                    "%s %s",
                    mainActivity.getString(R.string.app_version),
                    BuildConfig.VERSION_NAME
            ));
        }

        /**
         * Initialising ListView Netlist
         * @param mainActivity MainActivity
         */
        public static NetlistAdapter initNetlist(final MainActivity mainActivity) {
            final ListView netlist = mainActivity.findViewById(R.id.netList_listView);
            final NetlistAdapter netlistAdapter = new NetlistAdapter(mainActivity);

            netlist.setAdapter(netlistAdapter);
            netlist.setOnItemClickListener(mainActivity);
            netlist.setOnItemLongClickListener(mainActivity);

            return netlistAdapter;
        }

        public static void initAutoconnectCheckbox(final MainActivity mainActivity) {
            ((CheckBox) mainActivity.findViewById(R.id.netList_checkbox_autoconnect)).
                    setChecked(isAutoConnect(mainActivity));
        }
    }

    public static class Video {

        public static void init(final VideoActivity videoActivity) {
            videoActivity.initToolbar(
                    R.id.activity_video_toolbar,
                    R.string.activity_video,
                    true
            );
        }

    }

    public static class Instruction {
        public static void init(final InstructionActivity instructionActivity) {
            instructionActivity.initToolbar(
                    R.id.activity_instruction_toolbar,
                    R.string.activity_instruction,
                    true
            );
        }
    }

    public static class About {
        public static void init(final AboutActivity aboutActivity) {
            aboutActivity.initToolbar(
                    R.id.activity_about_toolbar,
                    R.string.activity_about,
                    true
            );
        }
    }

    public static class License {
        public static void init(final LicenseActivity licenseActivity) {
            licenseActivity.initToolbar(
                    R.id.activity_license_toolbar,
                    R.string.activity_license,
                    true
            );
        }
    }

    public static class Settings {

        public static void init(final SettingsActivity settingsActivity) {
            settingsActivity.initToolbar(
                    R.id.activity_settings_toolbar,
                    R.string.activity_settings,
                    true
            );
        }

    }

    public static class Feedback {
        public static void init(final FeedbackActivity feedbackActivity) {
            feedbackActivity.initToolbar(
                    R.id.activity_feedback_toolbar,
                    R.string.activity_feedback,
                    true
            );
        }

        public static Drawable getDisabledIcon(final FeedbackActivity feedbackActivity) {
            return ContextCompat.getDrawable(
                    feedbackActivity,
                    R.drawable.ic_action_send_msg_disabled
            );
        }

        public static Drawable getEnabledIcon(final FeedbackActivity feedbackActivity) {
            return ContextCompat.getDrawable(
                    feedbackActivity,
                    R.drawable.ic_action_send_msg_enabled
            );
        }
    }
}
