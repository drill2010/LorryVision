package name.marinchenko.lorryvision.activities.info;

import android.os.Bundle;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;

public class InstructionActivity extends ToolbarAppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        Initializer.Instruction.init(this);
    }
}
