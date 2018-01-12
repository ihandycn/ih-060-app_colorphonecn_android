package com.honeycomb.colorphone.resultpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.acb.utils.ConcurrentUtils;
import com.colorphone.lock.util.CommonUtils;
import com.colorphone.lock.util.PreferenceHelper;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Thunk;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class ResultPageActivity extends HSAppCompatActivity
        implements ResultPageContracts.View, INotificationObserver {

    public static final String TAG = "ResultPageActivity";

    // FIXME: Too much global states here. Needs serious refactoring.
    static class Globals {
        static long sLastResultPageUnfocusedTime;

        private static boolean sAttached;
    }

    /**
     * Notification sent when this activity becomes visible to user.
     * We shall start result page animations at this notification.
     */
    public static final String NOTIFICATION_VISIBLE_TO_USER = "result_page_visible_to_user";
    public static final String NOTIFICATION_RESULT_PAGE_ATTACHED = "result_page_attached_to_window";

    public static final String EXTRA_KEY_RESULT_TYPE = "EXTRA_KEY_RESULT_TYPE";
    public static final String EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE = "EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE";
    public static final String EXTRA_KEY_BATTERY_OPTIMAL = "EXTRA_KEY_BATTERY_OPTIMAL";
    public static final String EXTRA_KEY_BATTERY_EXTEND_HOUR = "EXTRA_KEY_BATTERY_EXTEND_HOUR";
    public static final String EXTRA_KEY_SCAN_IS_FILE_SCAN = "EXTRA_KEY_SCAN_IS_FILE_SCAN";
    public static final String EXTRA_KEY_BATTERY_EXTEND_MINUTE = "EXTRA_KEY_BATTERY_EXTEND_MINUTE";
    public static final String EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT = "EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT";
    public static final String EXTRA_KEY_SHOULD_START_TO_LAUNCHER = "EXTRA_KEY_SHOULD_START_TO_LAUNCHER";

    /**
     * Responsible for resolving {@link ResultController.Type} and performing ad preload if needed.
     */
    private ResultPagePresenter mPresenter;

    private AcbNativeAd mAd;
    private MenuItem mExitBtn;

    private int mResultType;
    private boolean mShouldStartToLauncher;
    private boolean mIsResultPageShow;

    /**
     * Responsible for doing actual animations.
     */
    private ResultController mResultController;
    private int mClearNotificationsCount;

    // make list static to restore last scanning result
    private static List<String> sAppList = new ArrayList<>();

    public static void startForBoost(Context context, int cleanedSizeMbs, boolean shouldStartToLauncher) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(context, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
        intent.putExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, cleanedSizeMbs);
        intent.putExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, shouldStartToLauncher);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        LauncherAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.BOOST_TOOLBAR);
    }

//    public static void startForBoostPlus(Activity activity, int cleanedSizeMbs, boolean shouldStartToLauncher) {
//        if (activity == null) {
//            return;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
//        intent.putExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, cleanedSizeMbs);
//        intent.putExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, shouldStartToLauncher);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        LauncherAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.BOOST_PLUS);
//    }
//
//    public static void startForBattery(Activity activity, boolean isBatteryOptimal,
//                                       int extendHour, int extendMinute) {
//        if (activity == null) {
//            return;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BATTERY);
//        intent.putExtra(EXTRA_KEY_BATTERY_OPTIMAL, isBatteryOptimal);
//        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR, extendHour);
//        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE, extendMinute);
//        boolean shouldStartToLauncher = BatteryUtils.shouldReturnToLauncherFromResultPage();
//        intent.putExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, shouldStartToLauncher);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        LauncherAnalytics.logEvent("ResultPage_Show", true, "Type", ResultConstants.BATTERY);
//    }
//
//    public static void startForJunkClean(Activity activity, boolean shouldStartToLauncher) {
//        if (activity == null) {
//            return;
//        }
//
//        if (JunkCleanConstant.sIsTotalSelected) {
//            JunkCleanConstant.sIsTotalCleaned = true;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_JUNK_CLEAN);
//        intent.putExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, shouldStartToLauncher);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        LauncherAnalytics.logEvent("ResultPage_Show", true, "Type", ResultConstants.JUNK_CLEANER);
//    }
//
//    public static void startForCpuCooler(Activity activity) {
//        if (activity == null) {
//            return;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_CPU_COOLER);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        LauncherAnalytics.logEvent("ResultPage_Show", true, "Type", ResultConstants.CPU_COOLER);
//    }
//
//    public static void startForNotificationCleaner(Activity activity, int clearNotificationsCount) {
//        if (activity == null) {
//            return;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER);
//        intent.putExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, clearNotificationsCount);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.none, R.anim.none);
//        LauncherAnalytics.logEvent("ResultPage_Show", true, "Type", ResultConstants.NOTIFICATION_CLEANER);
//    }
//
//    public static void startForVirusScan(Activity activity, boolean isFileScan) {
//        if (activity == null) {
//            return;
//        }
//
//        Intent intent = new Intent(activity, ResultPageActivity.class);
//        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_VIRUS_SCAN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra(EXTRA_KEY_SCAN_IS_FILE_SCAN, isFileScan);
//        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        LauncherAnalytics.logEvent("ResultPage_Show", true, "Type", isFileScan ? ResultConstants.FILE_SCAN : ResultConstants.VIRUS_SCAN);
//    }

//    private boolean shouldShowBoostPlusCard() {
//        long lastBoostPlusUsedTime = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
//                .getLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, -1);
//        long timeSinceLastUse = (System.currentTimeMillis() - lastBoostPlusUsedTime) / 1000 / 60;
//        int interval = HSConfig.optInteger(10, "Application", "ResultPage", "BoostCard", "ShowResultCardInterval");
//        return timeSinceLastUse > interval;
//    }
//
//    private void preloadMemInfo() {
//        DeviceUtils.getRunningPackageListFromMemory(false, new DeviceUtils.RunningAppsListener() {
//
//            @Override
//            public void onScanFinished(List<String> list, long l) {
//                sAppList.clear();
//                if (list != null) {
//                    sAppList.addAll(list);
//                }
//            }
//        });
//
//    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HSLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
//        if (shouldShowBoostPlusCard()) preloadMemInfo();

        mIsResultPageShow = false;
        onNewStart(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        HSLog.d(TAG, "onNewIntent");
        super.onNewIntent(intent);

        onNewStart(intent);
    }

    private void onNewStart(Intent intent) {

        if (null != intent) {
//            mResultType = intent.getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
            mResultType = intent.getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
            mShouldStartToLauncher = intent.getBooleanExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, false);
            mClearNotificationsCount = intent.getIntExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, 0);
            mPresenter = new ResultPagePresenter(this, mResultType);
            recordFeatureLastUsedTime();

//            NotificationCenter.addObserver(NOTIFICATION_VISIBLE_TO_USER, this);
        } else {
            finish();
        }

        // Set bg color early

//        if (mResultType == ResultConstants.RESULT_TYPE_CPU_COOLER
//                || mResultType == ResultConstants.RESULT_TYPE_BATTERY) {
//            ViewUtils.findViewById(this, R.id.bg_view).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
//        } else {
            ViewUtils.findViewById(this, R.id.bg_view).setBackgroundColor(getBackgroundColor());
//        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        HSLog.d(TAG, "onAttachedToWindow mResultType = " + mResultType + " mIsResultPageShow = " + mIsResultPageShow);
        super.onAttachedToWindow();
        Utils.setupTransparentStatusBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.container_view);
        viewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        if (!mIsResultPageShow) {
            mPresenter.show();
            mIsResultPageShow = true;
        }
        Globals.sAttached = true;
        ConcurrentUtils.postOnMainThread(new Runnable() {
            @Override public void run() {
                if (Globals.sAttached) {
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_RESULT_PAGE_ATTACHED);
                }
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseCurrentAd();
        Globals.sAttached = false;
    }

    public static boolean isAttached() {
        return Globals.sAttached;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_VISIBLE_TO_USER.equals(s)) {
            HSLog.d(TAG, NOTIFICATION_VISIBLE_TO_USER + " notified, start show mIsResultPageShow = " + mIsResultPageShow);
            if (!mIsResultPageShow) {
                mPresenter.show();
                mIsResultPageShow = true;
            }
        }
    }
//
//    public List<String> getScanAppList() {
//        if (sAppList.isEmpty()) {
//            List<String> fakeAppList = new ArrayList<>();
//            int size = new Random().nextInt(16) % 10 + 5;
//            for (AppInfo applicationInfo : LauncherAppState.getInstance().getModel().getAllAppsAppInfo()) {
//                if (fakeAppList.size() <= size) fakeAppList.add(applicationInfo.getPackageName());
//                else break;
//
//            }
//            return fakeAppList;
//        }
//        return sAppList;
//    }

    public @ColorInt int getBackgroundColor() {
//        switch (mResultType) {
//            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
//            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
//                return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
//            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
//                return ContextCompat.getColor(this, R.color.battery_green);
//            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
//                return ContextCompat.getColor(this, R.color.clean_primary_blue);
//            case ResultConstants.RESULT_TYPE_CPU_COOLER:
//                return ContextCompat.getColor(this, R.color.cpu_cooler_primary_blue);
//            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
//                return ContextCompat.getColor(this, R.color.notification_cleaner_green);
//        }
        return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
    }

    @Override
    public void show(ResultController.Type type, @Nullable AcbInterstitialAd interstitalAd, @Nullable AcbNativeAd ad, @Nullable List<CardData> cards) {
        String titleText;
        Intent intent = getIntent();
//        switch (mResultType) {
//            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
//                int cleanedSizeMbs = intent.getIntExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, 0);
//                mResultController = new BoostPlusResultController(this, cleanedSizeMbs, type, interstitalAd, ad, cards);
//                titleText = getString(R.string.launcher_widget_boost_plus_title);
//                break;
//            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
//                cleanedSizeMbs = intent.getIntExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, 0);
//                mResultController = new BoostPlusResultController(this, cleanedSizeMbs, type, interstitalAd, ad, cards);
//                titleText = getString(R.string.boost_title);
//                break;
//            case ResultConstants.RESULT_TYPE_BATTERY:
//                boolean isBatteryOptimal = intent.getBooleanExtra(EXTRA_KEY_BATTERY_OPTIMAL, false);
//                int extendHour = intent.getIntExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR, 0);
//                int extendMinute = intent.getIntExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE, 0);
//                mResultController = new BatteryResultController(this, isBatteryOptimal, extendHour, extendMinute, type, interstitalAd, ad, cards);
//                titleText = getString(R.string.battery_title);
//                break;
//            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
//                mResultController = new JunkCleanResultController(this, type, interstitalAd, ad, cards);
//                titleText = getString(R.string.clean_title);
//                break;
//            case ResultConstants.RESULT_TYPE_CPU_COOLER:
//                mResultController = new CpuCoolerResultController(this, type, interstitalAd, ad, cards);
//                titleText = getString(R.string.promotion_max_card_title_cpu_cooler);
//                break;
//            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
//                mResultController = new NotificationCleanerResultController(this, type, interstitalAd, ad, cards, mClearNotificationsCount);
//                titleText = getString(R.string.notification_cleaner_title);
//                break;
//            case ResultConstants.RESULT_TYPE_VIRUS_SCAN:
//                try {
//                    Class virusCls = Class.forName("com.honeycomb.launcher.resultpage.VirusScanResultController");
//                    Constructor[] constructors = virusCls.getConstructors();
//                    mResultController = (ResultController) constructors[0].newInstance(this, type, interstitalAd, ad, cards);
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//                boolean isFileScan = intent.getBooleanExtra(EXTRA_KEY_SCAN_IS_FILE_SCAN, false);
//                if (isFileScan) {
//                    titleText = getString(getResources().getIdentifier("file_scan", "string", BuildConfig.APPLICATION_ID));
//                } else {
//                    titleText = getString(getResources().getIdentifier("virus_scan", "string", BuildConfig.APPLICATION_ID));
//                }
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported result type.");
//        }
        int cleanedSizeMbs = intent.getIntExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, 0);
        mResultController = new BoostPlusResultController(this, cleanedSizeMbs, type, interstitalAd, ad, cards);
        titleText = getString(R.string.boost_title);

        if (BuildConfig.DEBUG && mAd != null) {
            throw new IllegalStateException("mAd must be null");
        }
        releaseCurrentAd();
        mAd = ad;
        ActivityUtils.configSimpleAppBar(this, titleText,
                FontUtils.getTypeface(FontUtils.Font.ROBOTO_MEDIUM), Color.TRANSPARENT);
        Drawable backButton = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        backButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backButton);
        mResultController.startTransitionAnimation();
        PreferenceHelper.get(Constants.NOTIFICATION_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_RESULT_PAGE_SHOWN_COUNT);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_page, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        mExitBtn = menu.findItem(R.id.action_bar_exit);
        if (mExitBtn != null) {
            mExitBtn.setVisible(false);
            mExitBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override public boolean onMenuItemClick(MenuItem menuItem) {
                    ResultPageActivity.this.finishSelfAndParentActivity();
                    return false;
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override public void showExitBtn() {
        if (mExitBtn != null) {
            mExitBtn.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAndNotify();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAndNotify();
    }

    void finishAndNotify() {
//        if (mResultType == ResultConstants.RESULT_TYPE_JUNK_CLEAN
//                || mResultType == ResultConstants.RESULT_TYPE_BOOST_PLUS
//                || mResultType == ResultConstants.RESULT_TYPE_BOOST_TOOLBAR
//                || mResultType == ResultConstants.RESULT_TYPE_BATTERY) {
//            if (mShouldStartToLauncher) {
//                CommonUtils.startLauncher(HSApplication.getContext());
//            } else {
//                CommonUtils.startLauncherToPage(HSApplication.getContext(), -1);
//            }
//            mShouldStartToLauncher = false;
//        }
//
//        HSGlobalNotificationCenter.sendNotification(BatteryActivity.NOTIFICATION_FINISH_BATTERY_ACTIVITY);
        FloatWindowManager.getInstance().removeAllDialogs();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Globals.sLastResultPageUnfocusedTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        LauncherFloatWindowManager.getInstance().removePermissionGuide(false);
//        LauncherFloatWindowManager.getInstance().removeFloatButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsResultPageShow = false;
        HSGlobalNotificationCenter.removeObserver(this);
        if (mPresenter != null) {
            mPresenter = null;
        }
        if (mExitBtn != null) {
            mExitBtn = null;
        }
        if (mResultController != null) {
            mResultController = null;
        }
    }

    @Thunk void releaseCurrentAd() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
    }

    public void finishSelfAndParentActivity() {
        HSLog.d(ResultPageActivity.TAG, "ResultPageActivity finishSelfAndParentActivity");
//        sendBroadcast(new Intent(BaseCenterActivity.INTENT_NOTIFICATION_ACTIVITY_FINISH_ACTION));
        finish();
    }

    private void recordFeatureLastUsedTime() {
//        switch (mResultType) {
//            case ResultConstants.RESULT_TYPE_BATTERY:
//                PreferenceHelper.get(LauncherFiles.BATTERY_PREFS)
//                        .putLong(ResultConstants.PREF_KEY_LAST_BATTERY_USED_TIME, System.currentTimeMillis());
//                break;
//            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
//            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
//                PreferenceHelper.get(Constants.BOOST_PREFS)
//                        .putLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, System.currentTimeMillis());
//                break;
//            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
//                PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
//                        .putLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, System.currentTimeMillis());
//                break;
//            case ResultConstants.RESULT_TYPE_CPU_COOLER:
//                PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS)
//                        .putLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, System.currentTimeMillis());
//                break;
//            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
//                PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS)
//                        .putLong(ResultConstants.PREF_KEY_LAST_NOTIFICATION_CLEANER_USED_TIME, System.currentTimeMillis());
//                break;
//        }

        PreferenceHelper.get(Constants.NOTIFICATION_PREFS)
                .putLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, System.currentTimeMillis());
    }
}
