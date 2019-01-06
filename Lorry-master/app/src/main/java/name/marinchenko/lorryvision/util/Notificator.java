package name.marinchenko.lorryvision.util;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.services.NetScanService;
import name.marinchenko.lorryvision.util.threading.DefaultExecutorSupplier;
import name.marinchenko.lorryvision.util.threading.ToastThread;

import static android.app.Notification.DEFAULT_ALL;
import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_JUMP;
import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_NOTIFICATION_ALLOWED;
import static name.marinchenko.lorryvision.activities.main.SettingsFragment.PREF_KEY_SOUND;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;

/**
 * Static methods for creating and showing notifications.
 */

public class Notificator {

    private static final int NOTIFICATION_NET_FOUND_ID = 100;
    private static final int NOTIFICATION_NET_FOUND_JUMP_DELAY = 1000;

    private static final String NOTIFICATION_CHANNEL_ID = "lorry_vision_notification";
    private static final String NOTIFICATION_CHANNEL_TITLE = "LorryVision notification";

    private static final String TAG_WAKELOCK = "tag_wakelock";
    private static final String TAG_WAKELOCK_CPU = "tag_wakelock_cpu";

    public static void notifyNetDetected(final NetScanService netScanService) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (notificationAllowed(netScanService)) {
                            showNotification(netScanService, createNotification(netScanService));
                        }

                        if (isAppBackground(netScanService)) {
                            if (notificationAllowed(netScanService)
                                    && jumpToAppAllowed(netScanService)
                                    && isScreenOn(netScanService, true)
                                    && !isLocked(netScanService)) {

                                jumpToApp(netScanService, NOTIFICATION_NET_FOUND_JUMP_DELAY);
                            }
                        }
                        else jumpToMainActivity(netScanService);
                    }
                }
        );
    }

    private static boolean isAppBackground(final Context context) {
        final NetScanService service = (NetScanService) context;

        long endTime = System.currentTimeMillis() + NOTIFICATION_NET_FOUND_JUMP_DELAY;
        while (System.currentTimeMillis() < endTime) {
            synchronized (context) {
                try {
                    context.wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }

        return service.isMessengerNull();
    }

    public static void removeNetDetectedNotification(final Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final NotificationManager mNotificationManager = (NotificationManager)
                                context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (mNotificationManager != null) {
                            mNotificationManager.cancel(NOTIFICATION_NET_FOUND_ID);
                        }
                    }
                }
        );
    }

    private static void jumpToApp(final NetScanService service,
                                  final int delay) {
        final Timer timer = new Timer();
        final TimerTask jumpTask = new TimerTask() {
            @Override
            public void run() {
                final PackageManager manager = service.getPackageManager();
                try {
                    final Intent intent = manager.getLaunchIntentForPackage(
                            service.getApplicationContext().getPackageName()
                    );
                    if (intent == null) return;
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                    if (service.isMessengerNull()) service.startActivity(intent);

                } catch (ActivityNotFoundException e) {
                    Log.w("MyLog", e.getMessage());
                }
            }
        };

        timer.schedule(jumpTask, delay);
    }

    public static void jumpToMainActivity(final NetScanService netScanService) {
        final Message msg = Message.obtain(null, MSG_RETURN_TO_MAIN);
        netScanService.sendMessage(msg);

        final Timer timer = new Timer();
        final TimerTask jumpTask = new TimerTask() {
            @Override
            public void run() {
                final Message msg = Message.obtain(null, MSG_RETURN_TO_MAIN);
                netScanService.sendMessage(msg);
            }
        };

        timer.schedule(jumpTask, NOTIFICATION_NET_FOUND_JUMP_DELAY);
    }

    public static boolean isLocked(final Context context) {
        final KeyguardManager keyguardManager = (KeyguardManager)
                context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
    }

    private static boolean notificationAllowed(final Context context) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_NOTIFICATION_ALLOWED, true);
    }

    private static boolean jumpToAppAllowed(final Context context) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_JUMP, true);
    }

    private static boolean soundAllowed(final Context context) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_SOUND, true);
    }

    private static void showNotification(final Context context,
                                         final Notification notification) {
        final NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION_NET_FOUND_ID, notification);
        }
    }

    private static Notification createNotification(final Context context) {
        final PackageManager manager = context.getPackageManager();
        final Intent jumpIntent = manager.getLaunchIntentForPackage(
                context.getApplicationContext().getPackageName()
        );
        if (jumpIntent != null) {
            jumpIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        }

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                jumpIntent,
                0
        );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context.getApplicationContext(),
                NOTIFICATION_CHANNEL_ID
        );

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle(context.getString(R.string.notif_net_found_content_title));
        mBuilder.setContentText(context.getString(R.string.notif_net_found_content_text));
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (soundAllowed(context)) mBuilder.setDefaults(DEFAULT_ALL);

        return mBuilder.build();
    }

    public static boolean isScreenOn(final Context context,
                                     final boolean turnOn) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isScreenOn()) {
            if (turnOn) turnScreenOn(pm);
            return false;
        }
        return true;
    }

    private static void turnScreenOn(final PowerManager pm) {
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE,
                TAG_WAKELOCK
        );
        wakeLock.acquire(0);

        PowerManager.WakeLock wakeLockCpu = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                TAG_WAKELOCK_CPU
        );
        wakeLockCpu.acquire(0);
    }
}
