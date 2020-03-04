package com.colorphone.smartlocker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.telephony.TelephonyManager;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.smartlocker.baidu.BaiduFeedManager;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.utils.DailyNewsUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class SmartLockerManager {

    private static final String TAG = "CHARGING_SCREEN_MANAGER";

    @IntDef({EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF, EXTRA_VALUE_START_BY_CHARGING_PLUG_IN, EXTRA_VALUE_START_BY_LOCKER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StartType {
    }

    public static final int EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF = 0;
    public static final int EXTRA_VALUE_START_BY_CHARGING_PLUG_IN = 1;
    public static final int EXTRA_VALUE_START_BY_LOCKER = 2;

    public static final String EXTRA_START_TYPE = "EXTRA_START_TYPE";

    private int startType;

    private volatile static SmartLockerManager sInstance;

    public static SmartLockerManager getInstance() {
        if (sInstance == null) {
            sInstance = new SmartLockerManager();
        }

        return sInstance;
    }

    public void tryToStartChargingScreenOrLockerActivity(@StartType int startType) {
        TelephonyManager telephonyManager = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getCallState()) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                return;
        }

        LockerCustomConfig.getLogger().logEvent("news_chance");
        AutoPilotUtils.logLockerModeAutopilotEvent("news_chance");
        if (startType == EXTRA_VALUE_START_BY_LOCKER) {
            AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_should_show");
        } else {
            AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_should_show");
        }
        tryToStartSmartLockerFeeds(startType);
    }

    private void tryToStartSmartLockerFeeds(@StartType int startType) {
        if (!NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Network");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Network");
            }
            return;
        }
        JSONObject jsonObject = DailyNewsUtils.getLastNews(BaiduFeedManager.CATEGORY_ALL);
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

        Intent intent = new Intent(HSApplication.getContext(), SmartLockerFeedsActivity.class);
        intent.putExtra(SmartLockerFeedsActivity.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                HSChargingManager.getInstance().getBatteryRemainingPercent());
        intent.putExtra(EXTRA_START_TYPE, startType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        try {
            HSApplication.getContext().startActivity(intent);
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Success");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryToPreLoadBaiduNews() {
        BaiduFeedManager.getInstance().loadNews(BaiduFeedManager.CATEGORY_ALL, BaiduFeedManager.LOAD_FIRST, new BaiduFeedManager.DataBackListener() {
            @Override
            public void onDataBack(JSONObject response) {
                HSLog.d(TAG, "tryToPreLoadBaiduNews onDataBack response success? " + (response != null));
                if (response != null) {
                    DailyNewsUtils.saveNews(BaiduFeedManager.CATEGORY_ALL, response.toString());
                }
            }
        });
    }

    public int getStartType() {
        return startType;
    }

    public void setStartType(int startType) {
        this.startType = startType;
    }
}
