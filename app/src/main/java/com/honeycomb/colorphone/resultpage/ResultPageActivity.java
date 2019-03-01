package com.honeycomb.colorphone.resultpage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.battery.BatteryUtils;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.AcbNativeAdAnalytics;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.List;



public class ResultPageActivity extends BaseAppCompatActivity
        implements ResultPageContracts.View, INotificationObserver {

    public static final String TAG = "ResultPageActivity";

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
    public static final String EXTRA_KEY_BATTERY_EXTEND_MINUTE = "EXTRA_KEY_BATTERY_EXTEND_MINUTE";
    public static final String EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT = "EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT";
    public static final String EXTRA_KEY_SHOULD_START_TO_LAUNCHER = "EXTRA_KEY_SHOULD_START_TO_LAUNCHER";

    public static final String PREF_KEY_INTO_BATTERY_PROTECTION_COUNT = "into_battery_protection_count";
    public static final String PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT = "into_notification_cleaner_count";
    public static final String PREF_KEY_INTO_APP_LOCK_COUNT = "into_app_lock_count";

    public static final int INTO_RESULT_PAGE_COUNT_NULL = -1;
    public static final int BATTERY_PROTECTION_LIMIT_COUNT = 1;
    public static final int NOTIFICATION_CLEANER_LIMIT_COUNT = 1;
    public static final int APP_LOCK_LIMIT_COUNT = 1;

    /**
     * Responsible for resolving {@link ResultController.Type} and performing ad preload if needed.
     */
    private ResultPagePresenter mPresenter;

    private int mResultType;
    private boolean mIsResultPageShow;

    /**
     * Responsible for doing actual animations.
     */
    private ResultController mResultController;

    private static boolean sAttached;
    private int mClearNotificationsCount;
    private boolean isPaused;

    public static void startForBoost(Context context, int cleanedSizeMbs) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PUSH);
        intent.putExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, cleanedSizeMbs);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startForBoostPlus(Activity activity, int cleanedSizeMbs, int resultType) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, resultType);
        intent.putExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, cleanedSizeMbs);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
//        activity.overridePendingTransition(0, 0);
    }

    public static void startForBattery(Context context, boolean isBatteryOptimal, int extendHour, int extendMinute) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(context, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BATTERY);
        intent.putExtra(EXTRA_KEY_BATTERY_OPTIMAL, isBatteryOptimal);
        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR, extendHour);
        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE, extendMinute);
        boolean shouldStartToLauncher = BatteryUtils.shouldReturnToLauncherFromResultPage();
        intent.putExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, shouldStartToLauncher);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        }
    }

    public static void startForCpuCooler(Activity activity) {
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_CPU_COOLER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }

    private void recordIntoBpAndNcCardTimes() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HSLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Utils.showWhenLocked(this);

        AcbInterstitialAdManager.getInstance().setForegroundActivity(this);

        setContentView(R.layout.result_page_activity);

        recordIntoBpAndNcCardTimes();

        Intent intent = getIntent();
        if (null != intent) {
            mResultType = intent.getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
            mClearNotificationsCount = intent.getIntExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, 0);
            mPresenter = new ResultPagePresenter(this, mResultType);

        } else {
            finish();
        }

        // Set bg color early
        ViewUtils.findViewById(this, R.id.bg_view).setBackgroundColor(getBackgroundColor());
    }


    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        HSLog.d(TAG, "onAttachedToWindow mResultType = " + mResultType + " mIsResultPageShow = " + mIsResultPageShow);
        super.onAttachedToWindow();
//        setupTransparentSystemBarsForLmp(this);
//        View viewContainer = Utils.findViewById(this, R.id.view_container);
//        viewContainer.setPadding(0, getStatusBarHeight(this), 0, 0);
        sAttached = true;
        if (!mIsResultPageShow) {
            mPresenter.show(ResultPageManager.getInstance().getAd());
            mIsResultPageShow = true;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sAttached = false;
    }

    public static boolean isAttached() {
        return sAttached;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_VISIBLE_TO_USER.equals(s)) {
            HSLog.d(TAG, NOTIFICATION_VISIBLE_TO_USER + " notified, start show mIsResultPageShow = " + mIsResultPageShow);

        }
    }

    public @ColorInt int getBackgroundColor() {
        if (ResultConstants.isResultBoost(mResultType)) {
            return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
        }
        switch (mResultType) {
            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
                return ContextCompat.getColor(this, R.color.battery_green);
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                return ContextCompat.getColor(this, R.color.clean_primary_blue);
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                return ContextCompat.getColor(this, R.color.cpu_cooler_primary_blue);
            default:
                return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
        }
    }

    public void show(ResultController.Type type, @Nullable List<CardData> cards) {
        String titleText;
        int titleColor = Color.WHITE;
        Intent intent = getIntent();
        if (ResultConstants.isResultBoost(mResultType)) {
            int cleanedSizeMbs = intent.getIntExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, 0);
            mResultController = new BoostPlusResultController(this, mResultType, cleanedSizeMbs, type, cards);
            titleText = getString(R.string.boost_title);
        } else if (mResultType == ResultConstants.RESULT_TYPE_BATTERY) {
            boolean isBatteryOptimal = intent.getBooleanExtra(EXTRA_KEY_BATTERY_OPTIMAL, false);
            int extendHour = intent.getIntExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR, 0);
            int extendMinute = intent.getIntExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE, 0);
            mResultController = new BatteryResultController(this, isBatteryOptimal, extendHour, extendMinute, type, cards);
            titleText = getString(R.string.battery_title);

        } else if (mResultType == ResultConstants.RESULT_TYPE_CPU_COOLER) {
            mResultController = new CpuCoolerResultController(this, type, cards);
            titleText = getString(R.string.promotion_max_card_title_cpu_cooler);

        } else {
            throw new IllegalArgumentException("Unsupported result type.");
        }


        ActivityUtils.configSimpleAppBar(this, titleText,
                FontUtils.getTypeface(FontUtils.Font.ROBOTO_MEDIUM), titleColor, Color.TRANSPARENT, false);

        TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setPadding(0, 0, 0, 0);
        startTransitionAnimation();
    }

    private void startTransitionAnimation() {
        if (null != mResultController) {
            mResultController.startTransitionAnimation();
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
        finish();
    }


    @Override
    protected void onResume() {
        isPaused = false;
        super.onResume();
        if (mResultController != null) {
            mResultController.notifyInterstitialAdClosedByCustomer();
        }
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsResultPageShow = false;
        boolean isAdShow = false;
        if (mResultController != null) {
            isAdShow = mResultController.isAdShown();
            mResultController.release();
        }
        AcbNativeAdAnalytics.logAppViewEvent(ResultPageManager.getInstance().getExpressAdPlacement(), isAdShow);

        ResultPageManager.getInstance().releaseAd();
        ResultPageManager.getInstance().releaseInterstitialAd();
        ResultPageManager.getInstance().markAdDirty();
        HSGlobalNotificationCenter.removeObserver(this);
    }

}
