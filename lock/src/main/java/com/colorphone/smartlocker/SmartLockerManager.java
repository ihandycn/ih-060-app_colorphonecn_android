package com.colorphone.smartlocker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.baidu.BaiduFeedManager;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.NewsUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;

import org.json.JSONObject;

import java.util.List;

public class SmartLockerManager {

    private static final String TAG = "CHARGING_SCREEN_MANAGER";

    public static final int EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF = 0;
    public static final int EXTRA_VALUE_START_BY_LOCKER = 2;
    private boolean exist = false;

    private int showNativeCount = 0;

    public static final String EXTRA_START_TYPE = "EXTRA_START_TYPE";

    private volatile static SmartLockerManager sInstance;

    public static SmartLockerManager getInstance() {
        if (sInstance == null) {
            sInstance = new SmartLockerManager();
        }

        return sInstance;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public void tryToStartChargingScreenOrLockerActivity(int startType) {
        TelephonyManager telephonyManager = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getCallState()) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                return;
        }

        LockerCustomConfig.getLogger().logEvent("news_chance");
        tryToStartSmartLockerFeeds(startType);
    }

    private void tryToStartSmartLockerFeeds(int startType) {
        if (!NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Network");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Network");
            }
            return;
        }

        if (!SmartLockerManager.isShowH5NewsLocker()) {
            JSONObject jsonObject = NewsUtils.getLastNews(BaiduFeedManager.CATEGORY_ALL);
            BaiduFeedItemsBean baiduFeedItemsBean = new BaiduFeedItemsBean(jsonObject);
            List<BaiduFeedBean> baiduFeedBeanList = baiduFeedItemsBean.getBaiduFeedBeans();
            int newsCount = 0;
            for (BaiduFeedBean baiduNewsItemData : baiduFeedBeanList) {
                if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE
                        || baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE) {
                    newsCount++;
                }
            }
            if (newsCount < 5) {
                if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                    LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Count");
                } else {
                    LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Count");
                }
                HSLog.d(TAG, "baiduFeedBeanList news count < 5");
                return;
            }
        }

        showLockerActivity(startType);
    }

    public void tryToPreLoadBaiduNews() {
        if (!NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
            LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Network");
            return;
        }
        BaiduFeedManager.getInstance().loadNews(BaiduFeedManager.CATEGORY_ALL, BaiduFeedManager.LOAD_REFRESH, new BaiduFeedManager.DataBackListener() {
            @Override
            public void onDataBack(JSONObject response) {
                if (response != null) {
                    NewsUtils.saveNews(BaiduFeedManager.CATEGORY_ALL, response.toString());

                    if (NewsUtils.getCountOfResponse(response.toString()) < 5) {
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Count");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Success");
                    }

                } else {
                    if (NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "ResponseNull");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Network");
                    }
                }
            }
        });
    }

    public void setShowAdCount(int count) {
        showNativeCount = count;
    }

    public int getShowAdCount() {
        return showNativeCount;
    }

    private static class NotificationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CANCEL_NOTIFICATION) {
                NotificationManager notificationManager = (NotificationManager) HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel("AA_TAG1", 10101);
            }
        }
    }

    private static final int MSG_CANCEL_NOTIFICATION = 100;
    private NotificationHandler handler = new NotificationHandler();

    private void showLockerActivity(int startType) {
        Context context = HSApplication.getContext();

        Intent intent = new Intent(HSApplication.getContext(), SmartLockerFeedsActivity.class);
        intent.putExtra(SmartLockerScreen.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                HSChargingManager.getInstance().getBatteryRemainingPercent());
        intent.putExtra(EXTRA_START_TYPE, startType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setAction("inner_action");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 10102, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        boolean sendSuccess = false;
        try {
            pendingIntent.send();
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Success");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Success");
            }
            sendSuccess = true;
        } catch (Exception ignore) {
        }
        if (!sendSuccess) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                    LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Success");
                } else {
                    LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Success");
                }
            } catch (Exception ignore) {
            }
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel(context, notificationManager);
        notificationManager.cancel("AA_TAG1", 10101);
        notificationManager.notify("AA_TAG1", 10101,
                new NotificationCompat.Builder(context, "OptimizerApplicationChannel")
                        .setSmallIcon(R.drawable.charging_screen_guide_close)
                        .setFullScreenIntent(pendingIntent, true)
                        .setCustomHeadsUpContentView(new RemoteViews(context.getPackageName(), R.layout.notification_external_content_no_icon_layout))
                        .build()
        );
        handler.removeMessages(MSG_CANCEL_NOTIFICATION);
        handler.sendEmptyMessageDelayed(MSG_CANCEL_NOTIFICATION, 1000L);
    }

    public static void setNotificationChannel(Context context, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26
                && notificationManager.getNotificationChannel("OptimizerApplicationChannel") == null) {
            NotificationChannel notificationChannel = new NotificationChannel("OptimizerApplicationChannel",
                    context.getString(R.string.notification_no_useful_msg), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.notification_no_useful_msg));
            notificationChannel.setLockscreenVisibility(-1);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationChannel.setSound(null, null);
            notificationChannel.setBypassDnd(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static boolean isShowH5NewsLocker() {
        return HSConfig.optBoolean(true, "Application", "NewsH5Locker", "CableH5Enable");
    }
}
