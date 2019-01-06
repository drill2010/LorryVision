package name.marinchenko.lorryvision.activities.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.util.Initializer;

/**
 * Settings fragment to use SharedPreferences.
 */

public class SettingsFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String PREF_KEY_LANGUAGE = "pref_key_language";

    public final static String PREF_KEY_AUTOCONNECT = "pref_key_autoconnect";
    public final static String PREF_KEY_AUTOUPDATE = "pref_key_autoupdate";
    public final static String PREF_KEY_DISPLAY_GENERAL = "pref_key_display_general";

    public final static String PREF_KEY_SOUND = "pref_key_sound";
    public final static String PREF_KEY_NOTIFICATION_ALLOWED = "pref_key_notification_allowed";
    public final static String PREF_KEY_JUMP = "pref_key_jump";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        onSharedPreferenceChanged(
                PreferenceManager.getDefaultSharedPreferences(getActivity()),
                PREF_KEY_LANGUAGE
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }

        if (key.equals(PREF_KEY_NOTIFICATION_ALLOWED)) {
            final CheckBoxPreference jump = (CheckBoxPreference) findPreference(PREF_KEY_JUMP);
            if (!sharedPreferences.getBoolean(PREF_KEY_NOTIFICATION_ALLOWED, true)) {
                sharedPreferences
                        .edit()
                        .putBoolean(PREF_KEY_JUMP, false)
                        .apply();
                jump.setChecked(false);
                jump.setEnabled(false);
            } else jump.setEnabled(true);
        }

        Initializer.initNetScanService(getActivity().getApplicationContext());
        Initializer.initAutoConnect(getActivity().getApplicationContext());
    }
}
