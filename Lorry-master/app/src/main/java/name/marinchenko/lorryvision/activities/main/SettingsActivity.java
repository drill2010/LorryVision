package name.marinchenko.lorryvision.activities.main;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;

public class SettingsActivity
        extends ToolbarAppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Initializer.Settings.init(this);

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_settings_fragment_frame, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
            case R.id.toolbar_popup_default_settings:
                resetToDefault();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetToDefault() {
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();

        refresh();
    }

    public void refresh() {
        final FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.activity_settings_fragment_frame, new SettingsFragment())
                .commit();
    }

}
