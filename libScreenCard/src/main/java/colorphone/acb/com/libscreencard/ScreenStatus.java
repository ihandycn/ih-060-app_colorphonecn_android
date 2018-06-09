package colorphone.acb.com.libscreencard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

public class ScreenStatus {

    private static final String TAG = ScreenStatus.class.getSimpleName();

    public static final String NOTIFICATION_PRESENT = "user_present";

    private static Runnable sPresentRunnable;

    static {
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    onUserPresent(context);
                }
            }
        }, filter);
    }

    public static boolean isScreenOn() {
        return isScreenOn(HSApplication.getContext());
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }


    public static void onUserPresent(Context context) {
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_PRESENT);
        if (sPresentRunnable != null) {
            sPresentRunnable.run();
        }
    }

    public static void setPresentRunnable(Runnable runnable) {
        sPresentRunnable = runnable;
        sHandler.removeCallbacksAndMessages(null);
        sHandler.sendEmptyMessageDelayed(EVENT_PRESENT_RUNNABLE_EXPIRE, 30 * DateUtils.SECOND_IN_MILLIS);
    }

    public static final int EVENT_PRESENT_RUNNABLE_EXPIRE = 1;
    @SuppressLint("HandlerLeak")
    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PRESENT_RUNNABLE_EXPIRE:
                    sPresentRunnable = null;
                    break;
                default:
                    break;
            }
        }
    };

}
