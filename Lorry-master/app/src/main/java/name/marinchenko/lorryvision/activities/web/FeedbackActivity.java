package name.marinchenko.lorryvision.activities.web;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.feedback.EmailValidator;
import name.marinchenko.lorryvision.util.feedback.FeedbackAgent;
import name.marinchenko.lorryvision.util.feedback.FeedbackMessage;

public class FeedbackActivity extends ToolbarAppCompatActivity {

    private Drawable iconDisabled;
    private Drawable iconEnabled;

    private EditText editTextEmail;
    private EditText editTextSubject;
    private EditText editTextMsg;
    private RadioGroup radioGroupType;

    private boolean msgOk = false;
    private boolean emailOk = false;

    private final FeedbackAgent feedbackAgent = new FeedbackAgent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Initializer.Feedback.init(this);

        this.editTextEmail = findViewById(R.id.activity_feedback_editText_email);
        this.editTextSubject = findViewById(R.id.activity_feedback_editText_subject);
        this.editTextMsg = findViewById(R.id.activity_feedback_editText_msg);
        this.radioGroupType = findViewById(R.id.activity_feedback_radioGroup_type);

        this.iconDisabled = Initializer.Feedback.getDisabledIcon(this);
        this.iconEnabled = Initializer.Feedback.getEnabledIcon(this);

        setEditTextListeners();
    }

    /**
     * Creating menu for toolbar
     * @param menu main menu
     * @return if created
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_feedback, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem send = menu.findItem(R.id.activity_feedback_toolbar_send);

        if (!send.isEnabled() && this.emailOk && this.msgOk) {
            send.setEnabled(true);
            send.setIcon(this.iconEnabled);
        } else {
            send.setEnabled(false);
            send.setIcon(this.iconDisabled);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Processing toolbar item selection
     * @param item selected item
     * @return if done
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_feedback_toolbar_send:
                saveMessage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setEditTextListeners() {
        this.editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                emailOk = EmailValidator.validate(editable.toString());
                invalidateOptionsMenu();
            }
        });

        this.editTextMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                msgOk = editable.length() > 0;
                invalidateOptionsMenu();
            }
        });
    }

    private void saveMessage() {
        this.editTextMsg.setText("");

        Toast.makeText(
                this,
                "Your message is saved and will be send when the Internet connection appears",
                Toast.LENGTH_LONG
        ).show();

        final FeedbackMessage msg = createMsg();
        this.feedbackAgent.add(this, msg);
    }

    private FeedbackMessage createMsg() {
        return new FeedbackMessage(
                this.editTextEmail.getText().toString(),
                this.editTextSubject.getText().toString(),
                selectedType(),
                this.editTextMsg.getText().toString()
        );
    }

    private String selectedType() {
        final int radioButtonID = this.radioGroupType.getCheckedRadioButtonId();
        final View radioButton = this.radioGroupType.findViewById(radioButtonID);
        final int idx = this.radioGroupType.indexOfChild(radioButton);
        final RadioButton r = (RadioButton) this.radioGroupType.getChildAt(idx);
        return r.getText().toString();
    }
}
