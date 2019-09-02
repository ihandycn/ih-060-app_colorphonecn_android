package com.honeycomb.colorphone.toolbar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.BuildCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArraySet;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.boost.RamUsageDisplayUpdater;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.battery.BatteryCleanActivity;
import com.honeycomb.colorphone.battery.BatteryUtils;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.boost.BoostIcon;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.cashcenter.CashUtils;
import com.honeycomb.colorphone.cpucooler.CpuCoolDownActivity;
import com.honeycomb.colorphone.cpucooler.CpuCoolerManager;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerConstant;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerUtils;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.AutoPilotUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.flashlight.FlashlightStatusListener;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NotificationManager implements FlashlightStatusListener {

    private static final int TOOLBAR_NOTIFICATION_ID = 0;
    public static final int NOTIFICATION_ID_FOREGROUND = 100;
    public static final int NOTIFICATION_ID_CLEAN_GUIDE = 2000;

    private static final int CLICK_DEBOUNCE_INTERVAL = 400;

    @ColorInt
    private static final int TRACK_COLOR = 0xff3f4043;

    public static final String ACTION_BOOST_TOOLBAR = "action_boost_toolbar";
    public static final String ACTION_WIFI_STATE_CHANGE = "action_wifi_state_change";
    private static final String ACTION_WIFI_CLICK = "action_wifi_click";
    public static final String ACTION_MOBILE_DATA = "action_data";
    public static final String ACTION_MOBILE_DATA_CHANGE = "action_data_change";
    private static final String ACTION_FLASH_LIGHT = "action_flash_light";
    public static final String ACTION_SETTINGS_CLICK = "action_settings_click";
    static final String ACTION_CPU_COOLER_TOOLBAR = "action_cpu_cooler_toolbar";
    static final String ACTION_BATTERY_TOOLBAR = "action_battery_toolbar";

    public static final String CLEAN_GUIDE_TYPE_BATTERY_LOW_ACTION = "battery_low_action";
    public static final String CLEAN_GUIDE_TYPE_CPU_HOT_ACTION = "cpu_hot_action";
    public static final String CLEAN_GUIDE_TYPE_BOOST_MEMORY_ACTION = "memory_action";
    public static final String CLEAN_GUIDE_TYPE_BOOST_JUNK_ACTION = "boost_junk_action";
    public static final String CLEAN_GUIDE_TYPE_BOOST_APPS_ACTION = "boost_apps_action";
    public static final String CLEAN_GUIDE_TYPE_BATTERY_APPS_ACTION = "battery_apps_action";
    public static final String CLEAN_GUIDE_DISMISS_ACTION = "clean_guide_dismiss_action";

    private static final String TAG = "NotificationManager";

    private  final String sNotificationChannelId = "notification_tool_bar";

    private static final int NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE = Dimensions.pxFromDp(30);

    private volatile static NotificationManager sInstance;

    private android.app.NotificationManager mNotificationManager;
    private RamUsageDisplayUpdater mRamUsageDisplayUpdater;
    private RemoteViews mRemoteViews;
    private Notification mNotificationToolbar;
    private Runnable mTurnOnRunnable;
    private Runnable mTurnOffRunnable;
    private boolean mRepeatCycle;
    private String mWifiString = "Wifi";

    private Bitmap mBoostIcon;
    private Canvas mBoostIconCanvas;
    private Paint mClearPaint;
    private boolean hasInsteadToolbarPush = false;

    private Notification foregroundNotification;

    private Map<String, Long> mLastClickMap = new HashMap<>(6);
    private SparseArray<Bitmap> mPreLollipopIconHolder;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public enum WifiDisplayState {
        ENABLED,
        ENABLING,
        DISABLED,
    }

    public static NotificationManager getInstance() {
        if (sInstance == null) {
            synchronized (NotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new NotificationManager();
                }
            }
        }
        return sInstance;
    }

    private NotificationManager() {
        FlashlightManager.getInstance().addListener(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mPreLollipopIconHolder = new SparseArray<>(16);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Set<String> existChannels = getAllExistingChannelIds(HSApplication.getContext());
            if (!existChannels.contains(sNotificationChannelId)) {
                createChannel(HSApplication.getContext());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel(@NonNull Context context) {
        NotificationChannel channel =
                new NotificationChannel(
                        sNotificationChannelId,
                        context.getText(R.string.notification_permission_toolbar_title),
                        android.app.NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(false);
        channel.enableVibration(false);
        channel.setVibrationPattern(new long[]{0});
        channel.setSound(null, null);
        context.getSystemService(android.app.NotificationManager.class).createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Set<String> getAllExistingChannelIds(@NonNull Context context) {
        Set<String> result = new ArraySet<>();
        android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
        for (NotificationChannel channel : notificationManager.getNotificationChannels()) {
            result.add(channel.getId());
        }
        return result;
    }

    RamUsageDisplayUpdater.RamUsageChangeListener mRamUsageChangeListener = new RamUsageDisplayUpdater.RamUsageChangeListener() {
        @Override
        public void onDisplayedRamUsageChange(int displayedRamUsage) {
            HSLog.d("ToolBar.Boost", "onDisplayedRamUsageChange usage = " + displayedRamUsage);
            updateBoostIcon(displayedRamUsage);
        }

        @Override
        public void onBoostComplete(int afterBoostRamUsage) {
            HSLog.d("ToolBar.Boost", "onBoostComplete usage = " + afterBoostRamUsage);
            updateBoostIcon(afterBoostRamUsage);
        }
    };

    public void showNotificationToolbarIfEnabled() {
        showNotificationToolbarIfEnabled(hasInsteadToolbarPush);
    }

    public void showNotificationToolbarIfEnabled(boolean hasOtherPush) {
        hasInsteadToolbarPush = hasOtherPush;
        if (hasOtherPush) {
            HSLog.d("ToolBar.Boost", "showNotificationToolbarIfEnabled has Other push, NOT show");
            return;
        }

        try {
            if (ModuleUtils.isNotificationToolBarEnabled() && UserSettings.isNotificationToolbarEnabled()) {
                Context context = HSApplication.getContext();
                initIfNeeded(context);
                notifySafely(TOOLBAR_NOTIFICATION_ID, mNotificationToolbar);
            } else {
                unregisterListeners();
                hideNotificationToolbar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterListeners() {
    }

    public void hideNotificationToolbar() {
        hideNotificationToolbar(false);
    }

    public void hideNotificationToolbar(boolean hasOtherPush) {
        if (hasOtherPush) {
            hasInsteadToolbarPush = true;
        }

        if (mNotificationManager == null) {
            Context context = HSApplication.getContext();
            mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(TOOLBAR_NOTIFICATION_ID);
    }

    private void initIfNeeded(Context context) {
        if (mNotificationManager == null) {
            mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (mRamUsageDisplayUpdater == null) {
            mRamUsageDisplayUpdater = RamUsageDisplayUpdater.getInstance();
            mRamUsageDisplayUpdater.addRamUsageChangeListener(mRamUsageChangeListener);
        }

        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_toolbar);
            mRemoteViews.setOnClickPendingIntent(R.id.boost_layout, getPendingIntent(ACTION_BOOST_TOOLBAR, true));
            mRemoteViews.setOnClickPendingIntent(R.id.cpu_cooler_layout, getPendingIntent(ACTION_CPU_COOLER_TOOLBAR, true));
            mRemoteViews.setOnClickPendingIntent(R.id.battery_layout, getPendingIntent(ACTION_BATTERY_TOOLBAR, true));

            mRemoteViews.setOnClickPendingIntent(R.id.flashlight_layout, getPendingIntent(ACTION_FLASH_LIGHT, false));
            mRemoteViews.setOnClickPendingIntent(R.id.clock_layout, getPendingIntent(ACTION_SETTINGS_CLICK, true));

            mBoostIcon = Bitmap.createBitmap(Dimensions.pxFromDp(64), Dimensions.pxFromDp(64), Bitmap.Config.ARGB_4444);
            mBoostIconCanvas = new Canvas(mBoostIcon);
            mClearPaint = new Paint();
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        // Settings or Cash-center
        if (CashUtils.checkGlobalSwitch()) {
            mRemoteViews.setImageViewResource(R.id.iv_clock, R.drawable.cash_icon_toolbar);
            mRemoteViews.setTextViewText(R.id.tv_clock, context.getString(R.string.spin));
        }

        boolean isForceUpdate = (mRemoteViews == null);

        initCpuCoolerView(mRemoteViews == null, isForceUpdate);
        initBatteryView(isForceUpdate);

        mRemoteViews.setImageViewBitmap(R.id.iv_boost, getBoostIcon(mRamUsageDisplayUpdater.getDisplayedRamUsage() / 100f));

        if (mNotificationToolbar == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(R.drawable.notification_toolbar_small_icon)
                    .setWhen(0)
                    .setSound(null)
                    .setContent(mRemoteViews);

            if (BuildCompat.isAtLeastO()) {
                builder.setChannelId(sNotificationChannelId);
            }
            mNotificationToolbar = builder.build();
        }

        if (mTurnOnRunnable == null) {
            mTurnOnRunnable = new Runnable() {
                @Override
                public void run() {
                    mRemoteViews.setImageViewResource(R.id.iv_flash_light, R.drawable.notification_toolbar_flashlight_on);
                    showNotificationToolbarIfEnabled();
                }
            };
        }

        if (mTurnOffRunnable == null) {
            mTurnOffRunnable = new Runnable() {
                @Override
                public void run() {
                    mRemoteViews.setImageViewResource(R.id.iv_flash_light, R.drawable.notification_toolbar_flashlight_off);
                    showNotificationToolbarIfEnabled();
                }
            };
        }

    }

    private volatile int mCpuTemperature;
    private int mCpuCoolerDrawableId;
    private volatile int mBatteryLevel;
    private int mBatteryDrawableId;

    private void initCpuCoolerView(boolean isFetchCpuTemperature, boolean isForceUpdateIcon) {
        if (null == mRemoteViews) {
            return;
        }
        if (isFetchCpuTemperature) {
            mCpuTemperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
        }

        int cpuResId = CpuCoolerManager.getInstance().getDrawableId(mCpuTemperature);
        HSLog.d(TAG, "Notification temperature = " + mCpuTemperature + " isFetchCpuTemperature = " + isFetchCpuTemperature + " mCpuCoolerDrawableId = " + mCpuCoolerDrawableId + " cpuResId = " + cpuResId + " isForceUpdateIcon = " + isForceUpdateIcon);
        if (mCpuCoolerDrawableId != cpuResId || isForceUpdateIcon) {
            mCpuCoolerDrawableId = cpuResId;
            setVectorDrawableForImageView(mRemoteViews, R.id.iv_cpu_cooler, mCpuCoolerDrawableId);
        }
    }

    private void initBatteryView(boolean isForceUpdateIcon) {
        if (null == mRemoteViews) {
            return;
        }
        int batteryLv = DeviceManager.getInstance().getBatteryLevel();
        if (mBatteryLevel != batteryLv || isForceUpdateIcon) {
            mBatteryLevel = batteryLv;
            mRemoteViews.setTextViewText(R.id.tv_battery_level, String.valueOf(batteryLv + "%"));
        }
        int resId = BatteryUtils.getToolbarBatteryResId(batteryLv, BatteryUtils.hasUserUsedBatteryRecently(5 * DateUtils.MINUTE_IN_MILLIS));
        HSLog.d(TAG, "Notification initBatteryView mBatteryLevel = " + mBatteryLevel + " batteryLv = " + batteryLv + " mBatteryDrawableId = " + mBatteryDrawableId + " resId = " + resId + " isForceUpdateIcon = " + isForceUpdateIcon);
        if (mBatteryDrawableId != resId || isForceUpdateIcon) {
            mBatteryDrawableId = resId;
            mRemoteViews.setImageViewResource(R.id.iv_battery, resId);
        }
    }

    @SuppressWarnings({"WeakerAccess", "RestrictedApi"})
    void setVectorDrawableForImageView(RemoteViews remoteViews, int viewId, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                remoteViews.setImageViewResource(viewId, drawableId);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            Drawable drawable;
            try {
                drawable = AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), drawableId);
            } catch (Exception e) {
                HSLog.e(TAG, e.getMessage());
                e.printStackTrace();
                drawable = ContextCompat.getDrawable(HSApplication.getContext(), R.drawable.empty);
            }
            Bitmap bitmap = getBitmapForIcon(viewId);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Center Inside
            float scale;
            float dx;
            float dy;
            int vwidth = canvas.getWidth();
            int vheight = canvas.getHeight();
            int dwidth = drawable.getIntrinsicWidth();
            int dheight = drawable.getIntrinsicHeight();

            if (dwidth <= vwidth && dheight <= vheight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) vwidth / (float) dwidth,
                        (float) vheight / (float) dheight);
            }
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);
            Matrix matrix  = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);

            canvas.save();
            canvas.concat(matrix);
            drawable.setBounds(0, 0, dwidth, dheight);
            drawable.draw(canvas);
            canvas.restore();

            remoteViews.setImageViewBitmap(viewId, bitmap);
        }
    }

    private Bitmap getBitmapForIcon(int viewId) {
        Bitmap bitmap = mPreLollipopIconHolder.get(viewId);
        if (bitmap == null) {
            // Generate one and only one bitmap per view ID
            bitmap = Bitmap.createBitmap(NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE,
                    NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE, Bitmap.Config.ARGB_8888);
            mPreLollipopIconHolder.put(viewId, bitmap);
        }
        return bitmap;
    }

    public void updateCpuCoolerCoolDown(int coolDownCpuTemperature) {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            if (0 != mCpuTemperature && mCpuTemperature > coolDownCpuTemperature) {
                mCpuTemperature -= coolDownCpuTemperature;
            }
            if (CpuCoolerUtils.shouldDisplayFahrenheit()) {
                int cpuTemperatureFahrenheit = (int) (Utils.celsiusCoolerByToFahrenheit(mCpuTemperature) + 0.5f);
                HSLog.d(TAG, "updateCpuCoolerCoolDown mCpuTemperature fahrenheit = " + cpuTemperatureFahrenheit);
            } else {
                HSLog.d(TAG, "updateCpuCoolerCoolDown mCpuTemperature Celsius = " + mCpuTemperature);
            }
            showNotificationToolbarIfEnabled();
        }
    }

    public void updateCpuCooler() {
        if (ScreenStatusReceiver.isScreenOn()
                && mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            mCpuTemperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
            if (CpuCoolerUtils.shouldDisplayFahrenheit()) {
                int cpuTemperatureFahrenheit = (int) (Utils.celsiusCoolerByToFahrenheit(mCpuTemperature) + 0.5f);
                HSLog.d(TAG, "updateCpuCooler mCpuTemperature fahrenheit = " + cpuTemperatureFahrenheit);
            } else {
                HSLog.d(TAG, "updateCpuCooler mCpuTemperature Celsius = " + mCpuTemperature);
            }
            initCpuCoolerView(false, false);
        }
    }

    public void updateBattery() {
        if (ScreenStatusReceiver.isScreenOn()
                && mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            showNotificationToolbarIfEnabled();
        }
    }

    public void autoUpdateCpuCoolerTemperature() {
        HSLog.d(TAG, "Notification autoUpdateCpuCoolerTemperature");
        mHandler.postDelayed(this::updateCpuCooler, CpuCoolerConstant.FROZEN_CPU_COOLER_SECOND_TIME * 500); // 30s, 1/2 FROZEN_CPU_COOLER_SECOND_TIME
    }

    @Override
    public void flashlightStatusChanged(boolean b) {
        if (mNotificationManager != null && mNotificationToolbar != null && mRemoteViews != null) {
            if (FlashManager.getInstance().isFlash()) {
                // When flash not handle notification status.
                return;
            }
            if (FlashManager.getInstance().isSOS()) {
                mRemoteViews.setImageViewResource(R.id.iv_flash_light, R.drawable.notification_toolbar_flashlight_on);
            } else {
                mRemoteViews.setImageViewResource(R.id.iv_flash_light, FlashlightManager.getInstance().isOn() ? R.drawable
                        .notification_toolbar_flashlight_on : R.drawable.notification_toolbar_flashlight_off);
            }
            showNotificationToolbarIfEnabled();
        }
    }

    public void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        HSLog.d("ToolBar.Click", "Click action = " + action);
        long currentTimeMillis = System.currentTimeMillis();
        Long lastClickTime = mLastClickMap.get(action);
        mLastClickMap.put(action, currentTimeMillis);
        if (lastClickTime == null) {
            lastClickTime = 0L;
        }
        if (currentTimeMillis - lastClickTime < CLICK_DEBOUNCE_INTERVAL && !TextUtils.equals(action,
                ACTION_WIFI_STATE_CHANGE) && !TextUtils.equals(action, ACTION_MOBILE_DATA_CHANGE)) {
            // In case of fast double click
            HSLog.d("ToolBar.Click", "fast double click, action = " + action);
            return;
        }
        switch (action) {
            case ACTION_WIFI_STATE_CHANGE:
                refreshWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
                break;
            case ACTION_WIFI_CLICK:
                toggleWifi();
                break;
            case ACTION_FLASH_LIGHT:
                int status = FlashlightUtils.toggleFlashlight(mTurnOnRunnable, mTurnOffRunnable);
                switch (status) {
                    case FlashlightUtils.FLASHLIGHT_STATUS_ON:
                        Analytics.logEvent("Notification_Toolbar_Flashlight_Clicked", "action", "open");
                        break;
                    case FlashlightUtils.FLASHLIGHT_STATUS_OFF:
                        Analytics.logEvent("Notification_Toolbar_Flashlight_Clicked", "action", "close");
                        break;
                    case FlashlightUtils.FLASHLIGHT_STATUS_FAIL:
                        Analytics.logEvent("Notification_Toolbar_Flashlight_Clicked", "action", "fail");
                        break;
                }
                AutoPilotUtils.logNotificationToolbarFlashlightClick();
                break;
            case NotificationManager.ACTION_BOOST_TOOLBAR:
                Analytics.logEvent("Notification_Toolbar_Boost_Clicked");
                Analytics.logEvent("Notification_Toolbar_Clicked");
                BoostActivity.start(context,  ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
                AutoPilotUtils.logNotificationToolbarBoostClick();
                break;
            case NotificationManager.ACTION_MOBILE_DATA:
                Navigations.startSystemDataUsageSetting(context, true);
                break;
            case NotificationManager.ACTION_SETTINGS_CLICK:
                // com.android.alarm.permission.SET_ALARM
                if (CashUtils.checkGlobalSwitch()) {
                    CashUtils.startWheelActivity(null, CashUtils.Source.Toolbar);
                    CashUtils.Event.logEvent("colorphone_earncash_icon_click_on_toolbar");
                } else {
                    Navigations.startActivitySafely(context, new Intent(Settings.ACTION_SETTINGS));
                    Analytics.logEvent("Notification_Toolbar_Settings_Clicked");
                    AutoPilotUtils.logNotificationToolbarSettingClick();
                }

                break;
            case NotificationManager.ACTION_CPU_COOLER_TOOLBAR:
                Analytics.logEvent("Notification_Toolbar_CPU_Clicked");
                Analytics.logEvent("Notification_Toolbar_Clicked");
                Intent cpuCoolerIntent = new Intent(context, CpuCoolDownActivity.class);
                cpuCoolerIntent.putExtra(CpuCoolDownActivity.EXTRA_KEY_NEED_SCAN, true);
                cpuCoolerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Navigations.startActivitySafely(context, cpuCoolerIntent);
                AutoPilotUtils.logNotificationToolbarCpuClick();
                break;

            case NotificationManager.ACTION_BATTERY_TOOLBAR:
                Analytics.logEvent("Notification_Toolbar_Battery_Clicked");
                Analytics.logEvent("Notification_Toolbar_Clicked");
                Intent batteryIntent = new Intent(context, BatteryCleanActivity.class);
                ResultPageManager.getInstance().setInBatteryImprover(false);
                batteryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Navigations.startActivitySafely(context, batteryIntent);
                AutoPilotUtils.logNotificationToolbarBatteryClick();
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_BATTERY_APPS_ACTION:
                Intent cleanBatteryAppIntent = new Intent(context, BatteryCleanActivity.class);
                cleanBatteryAppIntent.putExtra(BatteryCleanActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_BATTERY_CLEAN_GUIDE);
                Navigations.startActivitySafely(context, cleanBatteryAppIntent);
                showNotificationToolbarIfEnabled();

                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide6");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_BATTERY_LOW_ACTION:
                Intent cleanBatteryLowIntent = new Intent(context, BatteryCleanActivity.class);
                cleanBatteryLowIntent.putExtra(BatteryCleanActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_BATTERY_CLEAN_GUIDE);
                Navigations.startActivitySafely(context, cleanBatteryLowIntent);
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide1");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_BOOST_APPS_ACTION:
                BoostActivity.start(context, ResultConstants.RESULT_TYPE_BOOST_CLEAN_GUIDE);
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide5");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_BOOST_JUNK_ACTION:
                BoostActivity.start(context, ResultConstants.RESULT_TYPE_BOOST_CLEAN_GUIDE);
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide4");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_BOOST_MEMORY_ACTION:
                BoostActivity.start(context, ResultConstants.RESULT_TYPE_BOOST_CLEAN_GUIDE);
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide3");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case NotificationManager.CLEAN_GUIDE_TYPE_CPU_HOT_ACTION:
                Intent cpuHotIntent = new Intent(context, CpuCoolDownActivity.class);
                cpuHotIntent.putExtra(CpuCoolDownActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_CPU_CLEAN_GUIDE);
                Navigations.startActivitySafely(context, cpuHotIntent);
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Click", "Type", "Guide2");
                Analytics.logEvent("Clean_Guide_Close", "Type", "OKBtn");
                break;
            case CLEAN_GUIDE_DISMISS_ACTION:
                showNotificationToolbarIfEnabled();
                Analytics.logEvent("Clean_Guide_Close", "Type", "Slide");
                break;
            default:
                HSLog.w(TAG, "Unsupported action");
                break;
        }
    }

    private void refreshWifiState(int state) {

    }

    private void toggleWifi() {

    }

    private void updateBoostIcon(int ramUsage) {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            showNotificationToolbarIfEnabled();
        }
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse) {
        return getPendingIntent(action, autoCollapse, null);
    }

    public interface ExtraProvider {
        void onAddExtras(Intent intent);
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse, ExtraProvider extras) {
        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        if (autoCollapse) {
            Intent intent = new Intent(context, NotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (extras != null) {
                extras.onAddExtras(intent);
            }
            intent.setAction(action);
            return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent intent = new Intent(context, NotificationReceiver.class);
            if (extras != null) {
                extras.onAddExtras(intent);
            }
            intent.setAction(action);
            return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private Bitmap getBoostIcon(float percentage) {
        mBoostIconCanvas.drawPaint(mClearPaint);
        float centX = mBoostIcon.getWidth() / 2.0f;
        float centY = mBoostIcon.getHeight() / 2.0f;
        float radius = centX > centY ? centY : centX;
        radius -= Dimensions.pxFromDp(4f);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        mBoostIconCanvas.drawCircle(centX, centY, radius, paint);
        paint.setColor(TRACK_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.3f);
        float sweepAngle = 360 * percentage;
        RectF rectF = new RectF(centX - radius * 0.7f, centY - radius * 0.7f, centX + radius * 0.7f, centY + radius * 0.7f);
        mBoostIconCanvas.drawArc(rectF, 0, 360, false, paint);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(BoostIcon.getProgressColor((int) (percentage * 100)));
        mBoostIconCanvas.drawArc(rectF, -90, sweepAngle, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mBoostIconCanvas.drawCircle(centX, centY - radius * 0.7f, radius * 0.13f, paint);
        return mBoostIcon;
    }


    public static boolean notifySafely(int id, Notification notification) {
        android.app.NotificationManager notifyMgr = (android.app.NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notifyMgr.notify(id, notification);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean cancelSafely(int id) {
        android.app.NotificationManager notifyMgr = (android.app.NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notifyMgr.cancel(id);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        FlashlightManager.getInstance().removeListener(this);
        super.finalize();
    }

}
