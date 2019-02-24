package name.marinchenko.lorryvision.activities.info;

import android.os.Bundle;
import android.widget.TextView;

import name.marinchenko.lorryvision.BuildConfig;
import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;

public class AboutActivity extends ToolbarAppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Initializer.About.init(this);

        TextView tv = findViewById(R.id.activity_about_textView);
        tv.setText(String.format("Version : %s ", BuildConfig.VERSION_NAME));
    }
}
