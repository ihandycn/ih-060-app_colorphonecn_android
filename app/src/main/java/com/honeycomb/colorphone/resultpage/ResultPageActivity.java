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

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HSLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);

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
            mResultType = intent.getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
            mShouldStartToLauncher = intent.getBooleanExtra(EXTRA_KEY_SHOULD_START_TO_LAUNCHER, false);
            mClearNotificationsCount = intent.getIntExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, 0);
            mPresenter = new ResultPagePresenter(this, mResultType);
            recordFeatureLastUsedTime();

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

    public @ColorInt int getBackgroundColor() {
        return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
    }

    @Override
    public void show(ResultController.Type type, @Nullable AcbInterstitialAd interstitalAd, @Nullable AcbNativeAd ad, @Nullable List<CardData> cards) {
        String titleText;
        Intent intent = getIntent();
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
        finish();
    }

    private void recordFeatureLastUsedTime() {
        PreferenceHelper.get(Constants.NOTIFICATION_PREFS)
                .putLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, System.currentTimeMillis());
    }
}
