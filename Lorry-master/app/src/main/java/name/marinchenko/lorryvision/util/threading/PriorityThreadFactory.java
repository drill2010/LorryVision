package name.marinchenko.lorryvision.util.threading;


import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;


public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;

    public PriorityThreadFactory(final int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(@NonNull final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable t) {}
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}
