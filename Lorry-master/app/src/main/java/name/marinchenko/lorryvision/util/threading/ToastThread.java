package name.marinchenko.lorryvision.util.threading;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Creating toast from any thread.
 */

public class ToastThread {
    public static void postToastMessage(final Context context,
                                        final String message,
                                        final int lenght) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, lenght).show();
            }
        });
    }
}
