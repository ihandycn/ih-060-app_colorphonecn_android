package com.honeycomb.colorphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.honeycomb.colorphone.util.HSPermanentUtils;
import com.ihs.app.framework.HSApplication;

public class PermanentService extends Service {
    /**
     * 提供给库 Notification 和 NotificationID
     * Api 25(7.1) 版本，如果没有提供合法 Notification 和 NotificationID，PermanentService将不会 startForeground
     *
     * Service onCreate 后会回调 onServiceCreate 事件
     */
    public interface PermanentServiceListener {
        Notification getForegroundNotification();

        int getNotificationID();

        void onServiceCreate();
    }

    public static class PermanentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            L.line("onReceive:" + action + " keepAlive!");

            HSPermanentUtils.keepAlive();
        }
    }

    public static class PermanentServiceInner extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            try {
                startForeground(getNotificationID(), getDefaultNotification());
                stopForeground(true);
                stopSelf();
            } catch (Exception ignore) {

            }
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    public static final String ACTION_REFRESH_NOTIFICATION = "ACTION_REFRESH_NOTIFICATION";

    private static int getNotificationID() {
         return 50027;
    }

    private static Notification getDefaultNotification() {
        Notification foregroundNotification = null;
        try {
            if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
                foregroundNotification = new Notification(0, null, System.currentTimeMillis());
                foregroundNotification.flags |= Notification.FLAG_NO_CLEAR;
            } else {
                String packageName = HSApplication.getContext().getPackageName();
                Intent launchIntent = HSApplication.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                Intent resultIntent = new Intent(HSApplication.getContext(), launchIntent.getComponent().getClass());
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(HSApplication.getContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                foregroundNotification = createNotification("Guard Service", "Guard Service is Protect your device", null, -1, null, false,
                                                            resultPendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foregroundNotification;
    }

    private static Notification createNotification(String title, String content, String ticker, long date, Uri soundUri, boolean useDefaultSound,
                                                   PendingIntent pendingIntent) {
        Notification notification = null;
        try {
            if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
                notification = new Notification();
                if (!TextUtils.isEmpty(ticker)) {
                    notification.tickerText = ticker;
                }
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                if (null != soundUri) {
                    notification.sound = soundUri;
                } else if (useDefaultSound) {
                    notification.defaults |= Notification.DEFAULT_SOUND;
                }
                notification.icon = R.mipmap.ic_launcher;
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(HSApplication.getContext());
                if (!TextUtils.isEmpty(ticker)) {
                    builder.setTicker(ticker);
                }
                builder.setContentTitle(title);
                builder.setContentText(content);
                builder.setContentIntent(pendingIntent);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setAutoCancel(true);
                if (date > 0) {
                    builder.setWhen(date);
                }
                if (null != soundUri) {
                    builder.setSound(soundUri);
                }
                if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                    builder.setPriority(Notification.PRIORITY_MIN);
                }
                notification = builder.getNotification();
                if (null == soundUri && useDefaultSound) {
                    notification.defaults |= Notification.DEFAULT_SOUND;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notification;
    }

    private static Notification getNotification() {
        return null;
    }



    private boolean needUpdateNotification = true;
    private DeathRecipient deathRecipient = new DeathRecipient() {
        @Override
        public void binderDied() {
            L.l("service died, thread:" + Thread.currentThread().getName());
        }
    };

    private static class L {
        static void l(String name) {

        }

        public static void line(String s) {

        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Intent serviceIntent = new Intent(HSApplication.getContext(), PermanentService.class);
        bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                L.l("name:" + name + " service:" + service);
                try {
                    service.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    L.l("err:" + e.getMessage());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (needUpdateNotification) {
            needUpdateNotification = false;
            Notification notification = getNotification();
            if (notification != null) {
                startForeground(getNotificationID(), notification);
            } else if (VERSION.SDK_INT < 24) {
                Notification nf = getDefaultNotification();
                if (nf != null) {
                    startForeground(getNotificationID(), nf);
                }
                Intent serviceIntent = new Intent(HSApplication.getContext(), PermanentServiceInner.class);
                HSApplication.getContext().startService(serviceIntent);
            }
        }
        return START_REDELIVER_INTENT;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        L.l("-------onTaskRemoved:" + rootIntent);
        HSPermanentUtils.keepAlive();
    }

}