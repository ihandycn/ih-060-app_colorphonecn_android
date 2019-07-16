package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.VideoManager;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ColorPhoneApplicationImpl;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.PopularThemePreviewActivity;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.activity.ThemeSetHelper;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialer.guide.GuideSetDefaultActivity;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.preview.transition.GroupTransitionView;
import com.honeycomb.colorphone.preview.transition.TransitionActionLayout;
import com.honeycomb.colorphone.preview.transition.TransitionFadeView;
import com.honeycomb.colorphone.preview.transition.TransitionNavView;
import com.honeycomb.colorphone.preview.transition.TransitionView;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.DotsPictureView;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.view.RewardVideoView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Locale;

import hugo.weaving.DebugLog;

import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_SELECT;
import static com.honeycomb.colorphone.preview.ThemeStateManager.DOWNLOADING_MODE;
import static com.honeycomb.colorphone.preview.ThemeStateManager.ENJOY_MODE;
import static com.honeycomb.colorphone.preview.ThemeStateManager.INVALID_MODE;
import static com.honeycomb.colorphone.preview.ThemeStateManager.PREVIEW_MODE;
import static com.honeycomb.colorphone.preview.ThemeStateManager.WAIT_RINGTONE_MODE;


/**
 * Created by sundxing on 17/8/4.
 */

// TODO : clean Theme & Ringtone logic
public class ThemePreviewView extends FrameLayout implements ViewPager.OnPageChangeListener, INotificationObserver {

    private static final String TAG = ThemePreviewWindow.class.getSimpleName();
    private static final String PREF_KEY_SCROLL_GUIDE_SHOWN = "pref_key_scroll_guide_shown";

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final int MSG_PREVIEW = 1;
    private static final int MSG_ENJOY = 2;
    private static final int MSG_DOWNLOAD_OK = 11;

    private static final boolean PLAY_ANIMITION = true;
    private static final boolean NO_ANIMITION = false;

    public static final long ANIMATION_DURATION = 300;
    private static final long CHANGE_MODE_DURATION = 200;
    private static final long WINDOW_ANIM_DURATION = 400;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private static int[] sThumbnailSize = Utils.getThumbnailImageSize();
    public static Interpolator mInter = new AccelerateDecelerateInterpolator();

    private ThemePreviewWindow previewWindow;
    private InCallActionView mCallButtonView;

    private ThemePreviewActivity mActivity;

    private ProgressViewHolder mProgressViewHolder;
    private RingtoneViewHolder mRingtoneViewHolder;
    private ThemeSettingsViewHolder mThemeSettingsViewHolder;


    private TextView mApplyButton;
    private View mApplyForOne;
    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter intentFilter;
    private boolean themeLoading = false;

    private TransitionView mTransitionNavView;
    private TransitionView mTransitionActionLayout;
    private TransitionView mTransitionEnjoyLayout;

    private GroupTransitionView mTransitionCallView = new GroupTransitionView();

    private View mThemeInfoLayout;

    private ImageView previewImage;
    private Theme mTheme;
    private Type mThemeType;
    private View dimCover;

    private ViewGroup mUnLockButton;
    private RewardVideoView mRewardVideoView;

    private ThemeStateManager themeStateManager;

    private static final int THEME_ENJOY_UNFOLDING = 0;
    private static final int THEME_ENJOY_FOLDING = 1;
    /**
     * User set theme for someone success (Without ringtone).
     */
    public static boolean sThemeApplySuccessFlag = false;

    private TextView mThemeLikeCount;
    private TextView mThemeTitle;
    private TextView mThemeSelected;

    private PercentRelativeLayout rootView;


    private LottieAnimationView mThemeLikeAnim;

    private int foldingOrNot = THEME_ENJOY_FOLDING;

    // DownloadTask
    private SparseArray<DownloadTask> mDownloadTasks = new SparseArray<>(2);

    /**
     * If button is playing animation
     */
    private boolean inTransition;
    private boolean themeReady;

    private int mPosition = -1;
    private int mPageSelectedPos = -1;
    /**
     * Play no Transition animation when page scroll.
     * TODO remove Call views
     */
    private boolean mNoTransition = false;
    private boolean triggerPageChangeWhenIdle = false;
    /**
     * Normally, We block animation until page scroll idle, but
     * 1 first time that theme view show
     * 2 activity pause or resume
     * in those two conditions we start animation directly.
     */
    private boolean mBlockAnimationForPageChange = true;
    private boolean hasStopped;
    private boolean resumed;
    private boolean mWaitContactResult;
    private boolean mWaitForAll;
    private boolean mWindowInTransition;
    private boolean mPendingResume;

    private int mCurrentMode = INVALID_MODE;

    private long startDownloadTime;

    private int mWaitMediaReadyCount = 0;
    private ProgressHelper mProgressHelper = new ProgressHelper();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PREVIEW:
                    switchMode(PREVIEW_MODE);
                    HSLog.d(TAG, "MSG switchMode " + " [" + mTheme.getName());
                    themeStateManager.sendNotification(PREVIEW_MODE);
                    return true;

                case MSG_ENJOY:
                    switchMode(ENJOY_MODE);
                    themeStateManager.sendNotification(ENJOY_MODE);
                    return true;

                case MSG_DOWNLOAD_OK:
                    onMediaDownloadOK();
                    return true;
                default:
                    return false;

            }
        }
    });

    private StateChangeObserver observer = new StateChangeObserver() {
        @Override
        public void onReceive(int themeMode) {
            HSLog.d(TAG, "obeserver switchMode " + themeMode + " [" + mTheme.getName());
            switchMode(themeMode, false);
        }
    };

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            onTaskDownloaded(DownloadTask.TYPE_THEME);
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
            if (BuildConfig.DEBUG) {
                Toast.makeText(mActivity, "Paused!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
            final float percent = sofar
                    / (float) total;

            mProgressHelper.setProgressVideo((int) (percent * 100));
            mProgressViewHolder.updateProgressView(mProgressHelper.getRealProgress());
        }
    };

    DownloadStateListener mRingtoneDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            onTaskDownloaded(DownloadTask.TYPE_RINGTONE);
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
            // Do nothing
            if (BuildConfig.DEBUG) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("Ringtone", "Download failed : " + mTheme.getIdName() + ", progress: " + (int) (percent * 100));
            }
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
            final float percent = sofar
                    / (float) total;

            mProgressHelper.setProgressRingtone((int) (percent * 100));
            mProgressViewHolder.updateProgressView(mProgressHelper.getRealProgress());
            if (BuildConfig.DEBUG) {
                HSLog.d("Ringtone", "Downloading : " + mTheme.getIdName() + ", progress: " + (int) (percent * 100));
            }
        }
    };

    Runnable transEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSelectedPos() && !mBlockAnimationForPageChange) {
                resumeAnimation();
                mBlockAnimationForPageChange = true;
            }
        }
    };


    public static void saveThemeApplys(int themeId) {
        if (isThemeAppliedEver(themeId)) {
            return;
        }
        StringBuilder sb = new StringBuilder(4);
        String pre = HSPreferenceHelper.getDefault().getString(ThemeList.PREFS_THEME_APPLY, "");
        sb.append(pre).append(themeId).append(",");
        HSPreferenceHelper.getDefault().putString(ThemeList.PREFS_THEME_APPLY, sb.toString());
    }

    public static boolean isThemeAppliedEver(int themeId) {
        String[] themes = HSPreferenceHelper.getDefault().getString(ThemeList.PREFS_THEME_APPLY, "").split(",");
        for (String theme : themes) {
            if (TextUtils.isEmpty(theme)) {
                continue;
            }
            if (themeId == Integer.parseInt(theme)) {
                return true;
            }
        }
        return false;
    }

    public ThemePreviewView(@NonNull Context context) {
        super(context);
    }

    public ThemePreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemePreviewView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(ThemePreviewActivity activity, Theme theme, int position) {
        mActivity = activity;
        mTheme = theme;
        mPosition = position;
        ArrayList<Type> types = Type.values();
        for (Type t : types) {
            if (t.getValue() == mTheme.getId()) {
                mThemeType = t;
                break;
            }
        }

        activity.getLayoutInflater().inflate(R.layout.page_theme_preview, this, true);

        onCreate();
    }

    public void updateThemePreviewLayout(Type themeType) {
        previewWindow.updateThemeLayout(themeType);
        previewWindow.setAnimationVisible(INVISIBLE);

        // TODO may never visible
        TextView callName = findViewById(R.id.first_line);
        callName.setText(mTheme.getAvatarName());

        ImageView avatar = (ImageView) findViewById(R.id.caller_avatar);
        avatar.setImageDrawable(ContextCompat.getDrawable(mActivity, mTheme.getAvatar()));
        View callUserView = findViewById(R.id.led_call_container);
        callUserView.setVisibility(INVISIBLE);
        mTransitionCallView.addTranstionView(new TransitionFadeView(callUserView, CHANGE_MODE_DURATION));
    }

    public boolean isRewardVideoLoading() {
        if (mRewardVideoView != null && mRewardVideoView.isLoading()) {
            return true;
        }
        return false;
    }

    public boolean isRingtoneSettingShow() {
        return mRingtoneViewHolder.isRingtoneSettingsShow();
    }

    public void dismissRingtoneSettingPage() {
        mHandler.sendEmptyMessage(MSG_ENJOY);
        mRingtoneViewHolder.hideRingtoneSettings();
    }

    public void stopRewardVideoLoading() {
        if (mRewardVideoView != null) {
            mRewardVideoView.onHideAdLoading();
            mRewardVideoView.onCancel();
            mUnLockButton.setClickable(true);
        }
    }

    @DebugLog
    protected void onCreate() {
        previewWindow = (ThemePreviewWindow) findViewById(R.id.card_flash_preview_window);
        previewWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (themeReady
                        && !inTransition
                        && !mRingtoneViewHolder.isRingtoneSettingsShow()) {
                    if (getThemeMode() == PREVIEW_MODE) {
                        mHandler.sendEmptyMessage(MSG_ENJOY);

                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        if (foldingOrNot == THEME_ENJOY_UNFOLDING) {
                            mThemeSettingsViewHolder.foldView();
                        } else {
                            mHandler.sendEmptyMessage(MSG_PREVIEW);
                        }
                    }
                }
                if (mRingtoneViewHolder.isRingtoneSettingsShow()) {
                    dismissRingtoneSettingPage();
                }
            }
        });
        previewWindow.setAnimationVisible(INVISIBLE);
        themeStateManager = ThemeStateManager.getInstance();
        mCallButtonView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        mCallButtonView.setTheme(mThemeType);
        mCallButtonView.setAutoRun(false);
        mTransitionCallView.addTranstionView(new TransitionFadeView(mCallButtonView, CHANGE_MODE_DURATION));
        updateThemePreviewLayout(mThemeType);

        mApplyButton = (TextView) findViewById(R.id.theme_apply_btn);

        View actionLayout = findViewById(R.id.theme_apply_layout);
        mTransitionActionLayout = new TransitionActionLayout(actionLayout);

        View backView = findViewById(R.id.nav_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
            }
        });
        mTransitionNavView = new TransitionNavView(backView);

        mThemeInfoLayout = findViewById(R.id.card_theme_info_layout);
        mThemeInfoLayout.getLayoutParams().width = Math.max(Dimensions.pxFromDp(180), Dimensions.getPhoneWidth(mActivity) - Dimensions.pxFromDp(180));

        mApplyForOne = findViewById(R.id.theme_set_for_one);
        mApplyForOne.setEnabled(mTheme.getId() != Theme.RANDOM_THEME);

        mTransitionEnjoyLayout = new TransitionFadeView(findViewById(R.id.enjoy_layout), CHANGE_MODE_DURATION);

        mThemeTitle = findViewById(R.id.card_title);
        mThemeTitle.setText(mTheme.getName());

        TextView uploaderName = findViewById(R.id.uploader_name);
        if (!TextUtils.isEmpty(mTheme.getUploaderName())) {
            uploaderName.setVisibility(VISIBLE);
            uploaderName.setText("@" + mTheme.getUploaderName());
        }

        mThemeLikeCount = findViewById(R.id.card_like_count_txt);
        mThemeLikeCount.setText(String.valueOf(mTheme.getDownload()));
        mThemeLikeAnim = findViewById(R.id.like_count_icon);
        if (mTheme.isLike()) {
            mThemeLikeAnim.setProgress(1f);
        } else {
            mThemeLikeAnim.setProgress(0f);
        }
        setLikeClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTheme.setLike(!mTheme.isLike());
                if (mTheme.isLike()) {
                    mTheme.setDownload(mTheme.getDownload() + 1);
                } else {
                    mTheme.setDownload(mTheme.getDownload() - 1);
                }

                HSBundle bundle = new HSBundle();
                bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
                HSGlobalNotificationCenter.sendNotification(ThemePreviewActivity.NOTIFY_LIKE_COUNT_CHANGE, bundle);

                setLike(mTheme);

            }
        });

        mThemeSettingsViewHolder = new ThemeSettingsViewHolder();
        mThemeSelected = findViewById(R.id.card_selected);
        mThemeSelected.setVisibility(GONE);

        rootView = findViewById(R.id.root);


        // set background
        int color = getResources().getColor(R.color.black_80_transparent);
        int colorRipple = getResources().getColor(R.color.material_ripple);

        int r = Dimensions.pxFromDp(28);
        mApplyForOne.setBackground(BackgroundDrawables.createBackgroundDrawable(
                color, colorRipple,
                r, 0, 0, 0,
                false, true));
        mApplyButton.setBackground(BackgroundDrawables.createBackgroundDrawable(
                color, colorRipple,
                0, r, 0, 0,
                false, true));

        mProgressViewHolder = new ProgressViewHolder();
        mRingtoneViewHolder = new RingtoneViewHolder();
        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
        dimCover = findViewById(R.id.dim_cover);
        expandViewTouchDelegate(mThemeLikeAnim, Dimensions.pxFromDp(10), Dimensions.pxFromDp(37), Dimensions.pxFromDp(30), Dimensions.pxFromDp(72));
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inTransition) {
                    return;
                }
                if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
//                    PermissionChecker.getInstance().check(mActivity, "SetForAll");
                    StartGuideActivity.start(mActivity, StartGuideActivity.FROM_KEY_APPLY);
                    mWaitForAll = true;
                } else {
                    onApplyForAll();
                }
            }
        });

        mApplyForOne.setOnClickListener(v -> {
            if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
//                PermissionChecker.getInstance().check(mActivity, "SetForSomeone");
                StartGuideActivity.start(mActivity, StartGuideActivity.FROM_KEY_APPLY);
                mWaitForAll = false;
            } else {
                onApplyForOne();
            }
        });


    }

    private void onApplyForAll() {
        if (!mTheme.hasRingtone()) {
            onThemeApply();
        } else {
            mRingtoneViewHolder.setApplyForAll(true);
            showRingtoneSetButton();
        }

        if (mActivity instanceof PopularThemePreviewActivity) {
            Analytics.logEvent("Colorphone_BanboList_ThemeDetail_SetForAll");
            Analytics.logEvent("ColorPhone_BanboList_Set_Success");
        } else {
            Analytics.logEvent("ThemeDetail_SetForAll");
            Analytics.logEvent("ThemeDetail_SetForAll_Success");
        }
    }

    private void onApplyForOne() {
        Analytics.logEvent("Colorphone_SeletContactForTheme_Started", "ThemeName", mTheme.getIdName());
        if (mActivity instanceof PopularThemePreviewActivity) {
            ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_POPULAR_THEME);
            Analytics.logEvent("Colorphone_BanboList_ThemeDetail_SeletContactForTheme_Started");
        } else {
            Analytics.logEvent("ThemeDetail_SetForContact_Started");
            ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_MAIN);
        }

        mWaitContactResult = true;
    }

    private void playDownloadOkTransAnimation() {
        mProgressViewHolder.hide();
        dimCover.animate().alpha(0).setDuration(200);
    }

    private void onTaskDownloaded(int type) {
        clearTask(type);
        if (triggerMediaReady()) {
            long loadingDuration = System.currentTimeMillis() - startDownloadTime;
            if (startDownloadTime != 0) {
                Analytics.logEvent("ColorPhone_Theme_Download_Time", "Time",
                        String.valueOf((loadingDuration + 999) / DateUtils.SECOND_IN_MILLIS));
                startDownloadTime = 0;
            }

            // To ensure good effects, Loading animation limit min-duration 1s
            long loadingAnimDuration = System.currentTimeMillis() - mProgressViewHolder.getAnimationStartTimeMills();
            long delayTimeMills = DateUtils.SECOND_IN_MILLIS - loadingAnimDuration - 200;
            HSLog.d(TAG, "onTaskDownload, end duration = " + loadingAnimDuration);

            mHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_OK, delayTimeMills < 0 ? 0 : delayTimeMills);
        }
    }

    private void onMediaDownloadOK() {
        HSLog.d(TAG, "onTaskDownload, hide progress");
        playDownloadOkTransAnimation();
        onThemeReady(isSelectedPos());
    }

    private boolean triggerMediaReady() {
        mWaitMediaReadyCount--;
        return mWaitMediaReadyCount <= 0;
    }

    private void onVideoReady(boolean playTrans) {
        HSLog.d(TAG, "onVideoReady");
        mProgressHelper.setProgressVideo(100);
        if (triggerMediaReady()) {
            onThemeReady(playTrans);
        }
    }

    private void onRingtoneReady(boolean playTrans) {
        mProgressHelper.setProgressRingtone(100);
        HSLog.d(TAG, "onRingtoneReady");
        if (triggerMediaReady()) {
            onThemeReady(playTrans);
        }
    }

    /**
     * This called only when Music file and Video file all downloaded.
     * If no Music file here, this called same as {onVideoReady}
     *
     * @param needTransAnim
     */
    private void onThemeReady(boolean needTransAnim) {
        themeReady = true;
        themeLoading = false;

        setButtonState(isSelectedPos());

        dimCover.setVisibility(View.INVISIBLE);
        mProgressViewHolder.hide();

        // If user back from ringtone settings, we keep switch state as before.
        if (!mWaitContactResult) {
            // Video has audio
            if (mTheme.hasRingtone()) {
                mRingtoneViewHolder.setEnable(true);
                mRingtoneViewHolder.refreshMuteStatus();
            } else {
                mRingtoneViewHolder.setEnable(false);
                mRingtoneViewHolder.hideMusicSwitch();
            }
        }

        // Show overlay toast/guide view.
        if (sThemeApplySuccessFlag) {
            Utils.showApplySuccessToastView(rootView, mTransitionNavView);
            sThemeApplySuccessFlag = false;
        } else {
            if (isSelectedPos()) {
                checkVerticalScrollGuide();
            }
        }

        // Check view preview mode
        if (needShowRingtoneSetButton()) {
            mRingtoneViewHolder.setApplyForAll(false);
            showRingtoneSetButton();
            mWaitContactResult = false;
        } else {
            switchMode(getThemeMode(), needTransAnim);
        }

        if (needTransAnim) {
            playTransInAnimation(transEndRunnable);
        } else {
            transEndRunnable.run();
        }

    }

    private int getThemeMode() {
        return themeStateManager.getThemeMode();
    }

    @DebugLog
    private void onThemeApply() {
        saveThemeApplys(mTheme.getId());
        ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, mTheme.getId());
        // notify
        HSBundle bundle = new HSBundle();
        bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
        HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);

        Analytics.logEvent("ColorPhone_Set_Successed",
                "SetType", "SetForAll",
                "Theme", mTheme.getName(),
                "SetFrom", ThemeStateManager.getInstance().getThemeModeName());

        setButtonState(true);
        for (ThemePreviewView preV : mActivity.getViews()) {
            int viewPos = (int) preV.getTag();
            if (viewPos != mPageSelectedPos) {
                preV.updateButtonState();
            }
        }

        Utils.showApplySuccessToastView(rootView, mTransitionNavView);
        GuideSetDefaultActivity.start(mActivity, false);


        if (!TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Apply", mTheme.getIdName());
            Analytics.logEvent(event,
                    "RingtoneState", mRingtoneViewHolder.isMusicOn() ? "On" : "Off");
        }
        NotificationUtils.logThemeAppliedFlurry(mTheme);

        if (ConfigSettings.showAdOnApplyTheme()) {
            Ap.DetailAd.logEvent("colorphone_themedetail_choosetheme_ad_should_show");
            boolean show = AdManager.getInstance().showInterstitialAd();
            if (show) {
                Ap.DetailAd.logEvent("colorphone_themedetail_choosetheme_ad_show");
            }
        }

    }

    private boolean checkVerticalScrollGuide() {
        if (Preferences.getDefault().getBoolean(PREF_KEY_SCROLL_GUIDE_SHOWN, true)) {
            ViewStub stub = findViewById(R.id.preview_guide_viewstub);
            if (stub == null) {
                return false;
            }
            final View guideView = stub.inflate();
            guideView.setAlpha(0);
            guideView.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
            guideView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                        mActivity.findViewById(R.id.nav_back).setAlpha(1f);
                        guideView.animate().alpha(0).translationY(-Dimensions.getPhoneHeight(ThemePreviewView.this.getContext())).setDuration(ANIMATION_DURATION)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        guideView.setVisibility(GONE);
                                    }
                                }).start();
                        Preferences.getDefault().putBoolean(PREF_KEY_SCROLL_GUIDE_SHOWN, false);
                    }
                    return true;
                }
            });

            LottieAnimationView view = guideView.findViewById(R.id.theme_preview_guide_anim);
            view.useHardwareAcceleration();

            mActivity.findViewById(R.id.nav_back).setAlpha(0.1f);
            return true;
        }
        return false;
    }

    private void switchMode(int mode, boolean anim) {
        if (themeLoading) {
            return;
        }
        if (mode == mCurrentMode) {
            // Not change
            return;
        }
        mCurrentMode = mode;
        if (isRingtoneSettingShow()) {
            mRingtoneViewHolder.hideRingtoneSettings();
        }
        switch (mode) {
            case ENJOY_MODE:
                foldingOrNot = THEME_ENJOY_FOLDING;
                changeModeToEnjoy(anim);
                break;
            case PREVIEW_MODE:
                changeModeToPreview(anim);
                break;
            default:
                break;
        }
    }

    public void switchMode(int mode) {
        switchMode(mode, true);
    }

    private void setModeVisible(int mode, boolean visible) {
        switch (mode) {
            case ENJOY_MODE:
                if (visible) {
                    mTransitionEnjoyLayout.show(false);
                } else {
                    mTransitionEnjoyLayout.hide(false);
                }
                break;
            case PREVIEW_MODE:
                if (visible) {
                    mTransitionCallView.show(false);
                    mTransitionActionLayout.show(false);
                } else {
                    mTransitionCallView.hide(false);
                    mTransitionActionLayout.hide(false);
                }
                break;
            default:
                break;
        }
    }

    private void intoDownloadingMode() {
        mCurrentMode = DOWNLOADING_MODE;
        mTransitionEnjoyLayout.hide(false);
        mTransitionCallView.hide(false);
        mTransitionActionLayout.hide(false);
    }

    public void setLikeClick(View.OnClickListener onClickListener) {
        mThemeLikeCount.setOnClickListener(onClickListener);
        mThemeLikeAnim.setOnClickListener(onClickListener);
    }

    public void setLike(Theme theme) {
        if (mThemeLikeAnim.isAnimating()) {
            return;
        }
        if (theme.isLike()) {
            mThemeLikeAnim.playAnimation();
        } else {
            setLottieProgress(mThemeLikeAnim, 0f);
        }
        mThemeLikeCount.setText(String.valueOf(theme.getDownload()));
    }

    private static void setLottieProgress(LottieAnimationView animationView, float v) {
        if (animationView.getProgress() != v) {
            animationView.setProgress(v);
        }
    }

    private void changeModeToPreview(boolean anim) {
        boolean needAnim = isSelectedPos() && anim;
        mTransitionEnjoyLayout.hide(needAnim);

        // Show views for preview mode
        mTransitionCallView.show(needAnim);
        mTransitionActionLayout.show(needAnim);
    }

    private void changeModeToEnjoy(boolean anim) {
        boolean needAnim = isSelectedPos() && anim;

        if (ifThemeSelected()) {
            mThemeSelected.setVisibility(VISIBLE);
        } else {
            mThemeSelected.setVisibility(GONE);
        }

        fadeInActionView(needAnim);
        mTransitionEnjoyLayout.show(needAnim);
        mThemeSettingsViewHolder.reset();

        // Hide views for preview mode
        mTransitionCallView.hide(needAnim);
        mTransitionActionLayout.hide(needAnim);
    }

    private void animCallGroupViewToVisible(boolean visible) {
        boolean needAnim = isSelectedPos();

        if (visible) {
            mTransitionCallView.show(needAnim);
        } else {
            mTransitionCallView.hide(needAnim);
        }

        if (visible) {
            if (getThemeMode() == PREVIEW_MODE) {
                mTransitionActionLayout.show(needAnim);
            }
        } else {
            mTransitionActionLayout.hide(false);
        }
    }

    private Interpolator getmInterForTheme() {
        Interpolator mInterForTheme = PathInterpolatorCompat.create(0.175f, 0.885f, 0.32f, 1.275f);
        return mInterForTheme;
    }

    public boolean isThemeSettingShow() {
        return foldingOrNot == THEME_ENJOY_UNFOLDING;
    }

    public void returnThemeSettingPage() {
        mThemeSettingsViewHolder.foldView();
    }

    private void playTransInAnimation(final Runnable completeRunnable) {
        if (mNoTransition) {
            if (completeRunnable != null) {
                completeRunnable.run();
            }
        } else {
            // TODO trigger by callback
            mHandler.postDelayed(completeRunnable, WINDOW_ANIM_DURATION);
        }
    }

    private void setButtonState(final boolean curTheme) {
        if (curTheme) {
            mApplyButton.setText(getString(R.string.theme_current));
        } else {
            mApplyButton.setText(getString(R.string.theme_set_for_all));
        }
        mApplyButton.setEnabled(!curTheme);
    }

    private CharSequence getString(int id) {
        return mActivity.getString(id);
    }

    private void showRingtoneSetButton() {
        fadeOutActionView(isSelectedPos());
        mCurrentMode = WAIT_RINGTONE_MODE;
        setModeVisible(ENJOY_MODE, false);
        setModeVisible(PREVIEW_MODE, false);
        mRingtoneViewHolder.showRingtoneSettings();
    }

    private boolean needShowRingtoneSetButton() {
        if (!mTheme.hasRingtone()) {
            return false;
        }
        // If ringtone not set , show ringtone setting page
        if (mWaitContactResult
                && ThemeSetHelper.getCacheContactList() != null
                && !ThemeSetHelper.getCacheContactList().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Navation back view & rigtone view
     * @param anim
     */
    private void fadeInActionView(boolean anim) {
        mRingtoneViewHolder.transIn(true, anim && isSelectedPos());
        mTransitionNavView.show(anim && isSelectedPos());
    }

    /**
     * Navation back view & rigtone view
     * @param anim
     */
    private void fadeOutActionView(boolean anim) {
        mRingtoneViewHolder.transIn(false, anim && isSelectedPos());
        mTransitionNavView.hide(anim && isSelectedPos());
    }

    private boolean ifThemeSelected() {
        return ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
    }

    @DebugLog
    public void onStart() {
        mWaitMediaReadyCount = 0;
        // We do not play animation if activity restart.
        boolean playTrans = !hasStopped;

        boolean normalCreate = getThemeMode() == ENJOY_MODE;
        if (!normalCreate && !hasStopped) {
            // Recreate at other mode. No animation for smooth look.
            playTrans = false;
        }

        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(mTheme.getId());
        final TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(mTheme.getId());

        boolean hasRingtone = mTheme.hasRingtone() && ringtoneModel != null;
        boolean hasMediaVideo = model != null;
        if (hasRingtone) {
            mWaitMediaReadyCount++;
        }
        if (hasMediaVideo) {
            mWaitMediaReadyCount++;
        }

        mProgressHelper.setHasRingtoneProgress(hasRingtone);

        /**
         * Flag for theme loading, ringtone will loading with theme data.
         */
        if (model != null) {
            // GIf/Mp4
            if (TasksManager.getImpl().isDownloaded(model)) {
                onVideoReady(playTrans);
            } else {
                mDownloadTasks.put(DownloadTask.TYPE_THEME, new DownloadTask(model, DownloadTask.TYPE_THEME));
                themeLoading = true;
            }
        } else {
            // Directly applicable
            onThemeReady(playTrans);
        }

        if (hasRingtone) {
            if (TasksManager.getImpl().isDownloaded(ringtoneModel)) {
                onRingtoneReady(playTrans);
            } else {
                // Ringtone data not ready yet. If theme data not loads, we load ringtone separately.
                mDownloadTasks.put(DownloadTask.TYPE_RINGTONE, new DownloadTask(ringtoneModel, DownloadTask.TYPE_RINGTONE));
                themeLoading = true;
            }
        }

        if (themeLoading) {
            onThemeLoading();
            intoDownloadingMode();
        }

        // Show background if gif drawable not ready.
        if (mTheme != null) {
            if (!mThemeType.isMedia()) {
                previewImage.setImageDrawable(null);
                previewImage.setBackgroundColor(Color.BLACK);
            } else {

                boolean overrideSize = ColorPhoneApplicationImpl.mWidth > IMAGE_WIDTH;

                GlideRequest request = GlideApp.with(getContext())
                        .asBitmap()
                        .centerCrop()
                        .load(mTheme.getPreviewImage())
                        .diskCacheStrategy(DiskCacheStrategy.DATA);

                request.thumbnail(
                        GlideApp.with(getContext())
                                .asBitmap()
                                .load(mTheme.getPreviewImage())
                                .centerCrop()
                                .override(sThumbnailSize[0], sThumbnailSize[1])
                );

                if (overrideSize) {
                    request.override(IMAGE_WIDTH, IMAGE_HEIGHT);
                    request.skipMemoryCache(true);
                }
                request.listener(mRequestListener);

                request.into(previewImage);

            }

        }
    }

    RequestListener<Bitmap> mRequestListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            HSLog.d(TAG, "Picture onResourceReady");
            if (themeLoading) {
                mProgressViewHolder.setResource(resource);
            }
            return false;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
            return false;
        }
    };

    public void onStop() {
        hasStopped = true;
        pauseAnimation();

        mHandler.removeCallbacksAndMessages(null);

        for (int i = 0; i < mDownloadTasks.size(); i++) {
            DownloadTask downloadTask = mDownloadTasks.valueAt(i);
            FileDownloadMultiListener.getDefault().removeStateListener(downloadTask.getTasksManagerModel().getId());
        }

    }

    private void pauseAnimation() {
        if (themeReady) {
            previewWindow.stopAnimations();
            mCallButtonView.stopAnimations();
        }
        resumed = false;
    }

    /**
     * animation include loading progress and theme effects.
     */
    private void resumeAnimation() {
        if (mWindowInTransition) {
            mPendingResume = true;
            return;
        }

        resumed = true;

        if (themeReady) {
            previewWindow.setAnimationVisible(VISIBLE);
            previewWindow.playAnimation(mThemeType);
            mCallButtonView.doAnimation();
            if (mTheme.hasRingtone()) {
                mRingtoneViewHolder.refreshMuteStatus();
            }
        } else if (themeLoading) {
            HSLog.d(TAG, "onTaskDownload, resume");
            mProgressViewHolder.startLoadingAnimation();
        }

        if (mTheme != null && !TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Show", mTheme.getIdName());
            Analytics.logEvent(event);
        }
    }

    public boolean isSelectedPos() {
        return mPosition == mPageSelectedPos;
    }

    public void setPageSelectedPos(int pos) {
        mPageSelectedPos = pos;
    }

    private void onThemeLoading() {
        dimCover.setVisibility(View.VISIBLE);
        mProgressViewHolder.show();
//        updateThemePreviewLayout(mThemeType);
        playTransInAnimation(new Runnable() {
            @Override
            public void run() {
                // Download files
                for (int i = mDownloadTasks.size() - 1; i >= 0; i--) {
                    final DownloadTask task = mDownloadTasks.valueAt(i);
                    if (isTaskIdle(task)) {
                        // Direct start download tasks in current page
                        if (isSelectedPos()) {
                            download(task);
                        } else {
                            task.setStatus(DownloadTask.PENDING);
                        }
                    }
                }

                // Resume
                if (isSelectedPos()) {
                    resumeAnimation();
                }

            }
        });
    }

    private boolean isTaskIdle(DownloadTask task) {
        return task.getStatus() != DownloadTask.DOWNLOADING
                && task.getStatus() != DownloadTask.PENDING;
    }

    private void download(DownloadTask task) {
        HSLog.d(TAG, "onTaskDownload, start" + task.mType);
        startDownloadTime = System.currentTimeMillis();
        if (mTheme.isLocked()) {
            if (!mTheme.canBeDownloaded()) {
                mProgressViewHolder.hide();
                return;
            }
        }

        task.setStatus(DownloadTask.DOWNLOADING);
        if (task.isMediaTheme()) {
            downloadTheme(task.getTasksManagerModel());
        } else if (task.isRingtone()) {
            downloadRingtone(task.getTasksManagerModel());
        }
    }

    private void downloadTheme(TasksManagerModel model) {
        float percent = TasksManager.getImpl().getDownloadProgress(model.getId());
        if (percent == 0f || Float.isNaN(percent)) {
            ColorPhoneApplication.getConfigLog().getEvent().onThemeDownloadStart(model.getName().toLowerCase(), ConfigLog.FROM_DETAIL);
        }

        setBlockAnimationForPageChange(false);

        TasksManager.doDownload(model, null);

        // Notify download status.

        if (!mTheme.isLocked()) {
            HSBundle bundle = new HSBundle();
            bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
            HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_DOWNLOAD, bundle);
        }


        FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mDownloadStateListener);
    }

    private void downloadRingtone(TasksManagerModel model) {
        HSLog.d(TAG, "start download ringtone");
        TasksManager.doDownload(model, null);
        FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mRingtoneDownloadStateListener);
    }

    private void registerForInternetChange() {
        HSGlobalNotificationCenter.addObserver(StartGuideActivity.NOTIFICATION_PERMISSION_GRANT, this);

        themeStateManager.registerForThemeStateChange(observer);

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();

        getContext().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unregisterForInternetChange() {
        themeStateManager.unregisterForThemeStateChange(observer);
        HSGlobalNotificationCenter.removeObserver(this);
        getContext().unregisterReceiver(networkChangeReceiver);
    }

    private void clearTask(int taskType) {
        DownloadTask downloadTask = mDownloadTasks.get(taskType);
        if (downloadTask != null) {
            mDownloadTasks.remove(taskType);
            if (downloadTask.getTasksManagerModel() != null) {
                FileDownloadMultiListener.getDefault().removeStateListener(downloadTask.getTasksManagerModel().getId());
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        // Not called !!
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onVisibilityChanged = " + (visibility == VISIBLE));
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onAttachedToWindow");
        }
        super.onAttachedToWindow();

        registerForInternetChange();

        onStart();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onDetachedFromWindow");
        }
        onStop();

        unregisterForInternetChange();
        if (mRewardVideoView != null) {
            mRewardVideoView.onCancel();
        }
        super.onDetachedFromWindow();

    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(StartGuideActivity.NOTIFICATION_PERMISSION_GRANT, s)) {
            if (mWaitForAll) {
                onApplyForAll();
            } else {
                onApplyForOne();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onPageSelected " + position + "  " + mPageSelectedPos);
        }
        mPageSelectedPos = position;

        boolean isCurrentPageActive = isSelectedPos();

        if (isCurrentPageActive) {
            // Update download task
            if (mDownloadTasks != null) {
                for (int i = 0; i < mDownloadTasks.size(); i++) {
                    DownloadTask downloadTask = mDownloadTasks.valueAt(i);
                    if (downloadTask != null && downloadTask.getStatus() == DownloadTask.PENDING) {
                        download(downloadTask);
                    }
                }
            }

            if (mTheme.isLocked()) {
                Analytics.logEvent("Colorphone_Theme_Button_Unlock_show", "themeName", mTheme.getName());
            }

            // Notify others
            HSBundle bundle = new HSBundle();
            bundle.putInt("position", position);
            HSGlobalNotificationCenter.sendNotification(NotificationConstants.NOTIFICATION_PREVIEW_POSITION, bundle);
        }
        triggerPageChangeWhenIdle = true;
    }

    public void updateButtonState() {
        if (themeReady) {
            setButtonState(isCurrentTheme());
        }
    }

    private boolean isCurrentTheme() {
        return ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onPageScrollStateChanged " + state
                    + ", curSelect: " + mPageSelectedPos + ", trigger change: " + triggerPageChangeWhenIdle);
        }

        if (state == ViewPager.SCROLL_STATE_IDLE && triggerPageChangeWhenIdle) {
            triggerPageChangeWhenIdle = false;
            if (isSelectedPos()) {
                HSLog.d("onPageSelected " + mPosition);
                Analytics.logEvent("ColorPhone_ThemeDetail_View",
                        "ThemeName", mTheme.getIdName(),
                        "From", "Slide");
                Analytics.logEvent("ColorPhone_ThemeSwitch_Slide",
                        "PreviewMode", ThemeStateManager.getInstance().getThemeModeName());
                resumeAnimation();
            } else {
                HSLog.d("onPageUnSelected " + mPosition);
                pauseAnimation();
            }
        }

        // Check loading state
        if (themeLoading) {
            if (state == ViewPager.SCROLL_STATE_IDLE && isSelectedPos()) {
                mProgressViewHolder.mDotsPictureView.resumeAnimation();
            } else {
                mProgressViewHolder.mDotsPictureView.pauseAnimation();
            }
        }
    }


    public void setNoTransition(boolean noTransition) {
        mNoTransition = noTransition;
    }

    public void setBlockAnimationForPageChange(boolean blockAnimationForPageChange) {
        if (isSelectedPos()) {
            mBlockAnimationForPageChange = blockAnimationForPageChange;
        }
    }

    public void setWindowInTransition(boolean inTransition) {
        mWindowInTransition = inTransition;
    }

    public void onWindowTransitionStart() {
        mWindowInTransition = true;
        if (resumed) {
            if (themeLoading) {
                mProgressViewHolder.hide();
            }

            if (themeReady) {
                mThemeSettingsViewHolder.mEnjoyApplyBtn.animate().alpha(0).setDuration(200).start();
                // TODO
//                mNavBack.animate().alpha(0).setDuration(200).start();
                mRingtoneViewHolder.imageView.animate().alpha(0.1f)
                        .translationX(Dimensions.pxFromDp(28))
                        .translationY(-Dimensions.pxFromDp(26))
                        .setDuration(200).start();


                if (getThemeMode() == PREVIEW_MODE) {
                    animCallGroupViewToVisible(false);
                }

                // Layout in card item has less margins, smooth fade out
                mThemeInfoLayout.animate()
                        .translationX(-Dimensions.pxFromDp(12))
                        .translationY(Dimensions.pxFromDp(22))
                        .setDuration(200).start();
            }

        } else {
            if (themeReady) {
                mThemeSettingsViewHolder.mEnjoyApplyBtn.setAlpha(0.01f);
                mThemeSettingsViewHolder.mEnjoyApplyBtn.animate().alpha(1).setDuration(200).start();

                mRingtoneViewHolder.imageView.setTranslationX(Dimensions.pxFromDp(28));
                mRingtoneViewHolder.imageView.setTranslationY(-Dimensions.pxFromDp(28));
                mRingtoneViewHolder.imageView.setScaleX(0.76f);
                mRingtoneViewHolder.imageView.setScaleY(0.76f);
                mRingtoneViewHolder.imageView.animate()
                        .scaleX(1f).scaleY(1f)
                        .translationX(0).translationY(0)
                        .setDuration(200).start();

                // Layout in card item has less margins, smooth fade in
                mThemeInfoLayout.setTranslationX(-Dimensions.pxFromDp(12));
                mThemeInfoLayout.setTranslationY(Dimensions.pxFromDp(22));
                mThemeInfoLayout.animate().translationY(0).translationX(0).setDuration(200).start();
            }
        }
    }

    public void onWindowTransitionEnd() {
        mWindowInTransition = false;
        if (mPendingResume) {
            resumeAnimation();
            mPendingResume = false;
        }
    }

    private class ProgressHelper {
        int progressRingtone;
        int progressVideo;
        boolean hasRingtoneProgress;

        public void setHasRingtoneProgress(boolean hasRingtoneProgress) {
            this.hasRingtoneProgress = hasRingtoneProgress;
        }

        public void setProgressRingtone(int progressRingtone) {
            this.progressRingtone = progressRingtone;
        }

        public void setProgressVideo(int progressVideo) {
            this.progressVideo = progressVideo;
        }

        public int getRealProgress() {
            if (hasRingtoneProgress) {
                return (progressRingtone + progressVideo) / 2;
            } else {
                return progressVideo;
            }
        }
    }

    private class ProgressViewHolder {

        public DotsPictureView mDotsPictureView;
        private long mAnimationStartTimeMills;

        public ProgressViewHolder() {
        }

        private boolean inflateViewIfNeeded() {
            if (mDotsPictureView != null) {
                return false;
            }
            HSLog.d("ViewStub", "ProgressViewHolder");
            ViewStub stub = findViewById(R.id.stub_loading_animation);
            stub.inflate();
            mDotsPictureView = findViewById(R.id.dots_progress_view);
            return true;
        }

        public void updateProgressView(int percent) {
            // Do nothing
        }

        public void hide() {
            if (mDotsPictureView != null) {
                mDotsPictureView.setVisibility(View.INVISIBLE);
                mDotsPictureView.stopAnimation();
            }
        }

        public void show() {
            inflateViewIfNeeded();
            mDotsPictureView.setVisibility(VISIBLE);
        }

        public void setResource(Bitmap resource) {
            inflateViewIfNeeded();
            if (mDotsPictureView.getVisibility() == VISIBLE) {
                mDotsPictureView.setSourceBitmap(resource);
            }
        }

        public void startLoadingAnimation() {
            if (mDotsPictureView != null && mDotsPictureView.getVisibility() == VISIBLE) {
                HSLog.d(TAG, "startLoadingAnimation-" + mTheme.getName());
                boolean started = mDotsPictureView.startAnimation();
                if (started) {
                    mAnimationStartTimeMills = System.currentTimeMillis();
                }
            }
        }

        public long getAnimationStartTimeMills() {
            return mAnimationStartTimeMills;
        }
    }

    private class RingtoneViewHolder implements OnClickListener {
        private View imageView;
        private View ringtoneSetLayout;
        private View ringtoneChangeBtn;
        private View ringtoneKeepBtn;

        private Interpolator mPathInterpolator = PathInterpolatorCompat.create(
                0, 0, 0.34f, 0.99f);
        private float transYTop;
        private boolean isEnable;
        private boolean mApplyForAll;

        public RingtoneViewHolder() {
            imageView = findViewById(R.id.ringtone_image);
            imageView.setOnClickListener(this);
        }

        /**
         * Lazy inflate
         */
        private void inflateRingtoneSettingLayoutInNeed() {
            if (ringtoneSetLayout != null) {
                return;
            }

            ViewStub stub = findViewById(R.id.stub_theme_select_ringtone);
            stub.inflate();
            ringtoneSetLayout = findViewById(R.id.ringtone_apply_layout);
            ringtoneSetLayout.setVisibility(GONE);
            transYTop = getResources().getDimension(R.dimen.ringtone_apply_layout_height);

            ringtoneChangeBtn = findViewById(R.id.ringtone_apply_change);
            ringtoneChangeBtn.setOnClickListener(this);
            ringtoneChangeBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.white_87_transparent),
                    getResources().getColor(R.color.black_20_transparent),
                    Dimensions.pxFromDp(24), false, true));

            ringtoneKeepBtn = findViewById(R.id.ringtone_apply_keep);
            ringtoneKeepBtn.setOnClickListener(this);
            ringtoneKeepBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.white_87_transparent),
                    getResources().getColor(R.color.black_20_transparent),
                    Dimensions.pxFromDp(24), false, true));
        }

        public void showRingtoneSettings() {
            dimCover.setVisibility(VISIBLE);
            dimCover.setAlpha(0);
            dimCover.animate().alpha(1).setDuration(200);

            inflateRingtoneSettingLayoutInNeed();

            ringtoneSetLayout.setVisibility(VISIBLE);
            ringtoneSetLayout.setAlpha(1);
            ringtoneChangeBtn.setTranslationY(transYTop);
            ringtoneKeepBtn.setTranslationY(transYTop);
            ringtoneChangeBtn.animate().setDuration(240).setInterpolator(mPathInterpolator).translationY(0).start();
            ringtoneKeepBtn.animate().setDuration(240).setInterpolator(mPathInterpolator).translationY(0).setStartDelay(40).start();

            Analytics.logEvent("Ringtone_Set_Shown", "ThemeName", mTheme.getName());
        }

        public boolean isRingtoneSettingsShow() {
            return ringtoneSetLayout != null
                    && ringtoneSetLayout.getVisibility() == VISIBLE
                    && ringtoneSetLayout.getAlpha() > 0;
        }

        public void hideRingtoneSettings() {
            dimCover.animate().alpha(0).setDuration(200);
            if (ringtoneSetLayout != null) {
                ringtoneSetLayout.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ringtoneSetLayout.setVisibility(GONE);
                        dimCover.setVisibility(INVISIBLE);

                    }
                });
            }
        }

        public void setAsRingtone(boolean asRingtone, boolean resetDefault) {
            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    RingtoneHelper.ringtoneActive(mTheme.getId(), asRingtone);
                    if (resetDefault) {
                        RingtoneHelper.resetDefaultRingtone();
                    }
                    ContactManager.getInstance().updateRingtoneOnTheme(mTheme, asRingtone);
                }
            });
        }

        private void toggle() {
            final boolean currentSelect = imageView.isActivated();
            HSLog.d("Ringtone", "Switch to " + (currentSelect ? "Close" : "Open"));
            Analytics.logEvent("Ringtone_Action",
                    "Type", currentSelect ? "Close" : "Open",
                    "Theme", mTheme.getName());

            ThemeStateManager.getInstance().setAudioMute(currentSelect);
            if (currentSelect) {
                mute();
            } else {
                muteOff();
            }
        }

        private void hideMusicSwitch() {
            imageView.setVisibility(GONE);
        }

        private void mute() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(false);
            VideoManager.get().mute(true);
        }

        private void muteOff() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(true);
            VideoManager.get().mute(false);
        }

        private boolean isMusicOn() {
            return imageView.isActivated();
        }

        private void transIn(boolean in, boolean anim) {
            float offsetX = Dimensions.isRtl() ? -Dimensions.pxFromDp(60) : Dimensions.pxFromDp(60);
            float targetX = in ? 0 : offsetX;
            if (anim) {
                imageView.animate().translationX(targetX)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(mInter)
                        .start();
            } else {
                imageView.animate().cancel();
                imageView.setTranslationX(targetX);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ringtone_image:
                    toggle();
                    break;
                case R.id.ringtone_apply_change:
                    Analytics.logEvent("Ringtone_Video_Set_Success", "ThemeName", mTheme.getName());

                    hideRingtoneSettings();
                    if (mApplyForAll) {
                        // Ringtone enabled
                        RingtoneHelper.setDefaultRingtoneInBackground(mTheme);

                        onThemeApply();
                    } else {
                        setAsRingtone(true, false);

                        ThemeSetHelper.onConfirm(ThemeSetHelper.getCacheContactList(), mTheme, null);
                        Utils.showApplySuccessToastView(rootView, mTransitionNavView);

                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        mHandler.sendEmptyMessage(MSG_ENJOY);
                    } else {
                        mHandler.sendEmptyMessage(MSG_PREVIEW);
                    }
                    break;
                case R.id.ringtone_apply_keep:
                    Analytics.logEvent("Ringtone_Default_Set_Success", "ThemeName", mTheme.getName());

                    hideRingtoneSettings();
                    if (mApplyForAll) {
                        onThemeApply();
                    } else {
                        ThemeSetHelper.onConfirm(ThemeSetHelper.getCacheContactList(), mTheme, null);
                        Utils.showApplySuccessToastView(rootView, mTransitionNavView);
                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        mHandler.sendEmptyMessage(MSG_ENJOY);
                    } else {
                        mHandler.sendEmptyMessage(MSG_PREVIEW);
                    }
                    break;
                default:
                    break;
            }
        }

        public void setEnable(boolean enable) {
            isEnable = enable;
        }

        public boolean isEnable() {
            return isEnable;
        }

        public void setApplyForAll(boolean applyForAll) {
            mApplyForAll = applyForAll;
        }

        public void refreshMuteStatus() {
            if (ThemeStateManager.getInstance().isAudioMute()) {
                mute();
            } else {
                muteOff();
            }
        }
    }

    private class ThemeSettingsViewHolder {
        private TextView mEnjoyApplyBtn;
        private TextView mEnjoyApplyDefault;
        private TextView mEnjoyApplyForOne;
        private ImageView mEnjoyClose;

        public ThemeSettingsViewHolder() {
            mEnjoyApplyBtn = findViewById(R.id.theme_setting);
            mEnjoyApplyBtn.setTextColor(Color.WHITE);
            mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting);

            mEnjoyApplyBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Analytics.logEvent("ColorPhone_FullScreen_SetAsFlash_Clicked");
                    unFoldView();
                }
            });

            mEnjoyApplyDefault = findViewById(R.id.theme_setting_default);
            mEnjoyApplyForOne = findViewById(R.id.theme_setting_single);

            mEnjoyClose = findViewById(R.id.theme_setting_close);
            mEnjoyClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    foldView();
                }
            });

            mEnjoyApplyDefault.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inTransition) {
                        return;
                    }
                    if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                        PermissionChecker.getInstance().check(mActivity, "SetForAll");
                    }

                    if (!mTheme.hasRingtone()) {
                        onThemeApply();
                        mHandler.sendEmptyMessage(MSG_ENJOY);
                    } else {
                        mRingtoneViewHolder.setApplyForAll(true);
                        showRingtoneSetButton();
                    }

                    if (mActivity instanceof PopularThemePreviewActivity) {
                        Analytics.logEvent("Colorphone_BanboList_ThemeDetail_SetForAll");
                        Analytics.logEvent("ColorPhone_BanboList_Set_Success");
                    } else {
                        Analytics.logEvent("ThemeDetail_SetForAll");
                        Analytics.logEvent("ThemeDetail_SetForAll_Success");
                    }
                }
            });

            mEnjoyApplyForOne.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                        PermissionChecker.getInstance().check(mActivity, "SetForSomeone");
                    }

                    Analytics.logEvent("Colorphone_SeletContactForTheme_Started", "ThemeName", mTheme.getIdName());
                    if (mActivity instanceof PopularThemePreviewActivity) {
                        ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_POPULAR_THEME);
                        Analytics.logEvent("Colorphone_BanboList_ThemeDetail_SeletContactForTheme_Started");
                    } else {
                        Analytics.logEvent("ThemeDetail_SetForContact_Started");
                        ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_MAIN);
                    }

                    mWaitContactResult = true;
                }
            });

        }

        private void reset() {
            mEnjoyClose.setVisibility(GONE);
            mEnjoyApplyDefault.setVisibility(GONE);
            mEnjoyApplyForOne.setVisibility(GONE);

            mEnjoyApplyBtn.setScaleX(1.0f);
            mEnjoyApplyBtn.setAlpha(1);
            mEnjoyApplyBtn.setVisibility(VISIBLE);
            mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting);
        }
        private void unFoldView() {

            int startCoordinateDefault = Dimensions.pxFromDp(110);
            int endCoordinate = 0;
            mEnjoyApplyDefault.setTranslationY(startCoordinateDefault);
            mEnjoyApplyDefault.setAlpha(0);
            mEnjoyApplyDefault.animate().translationY(endCoordinate)
                    .alpha(1)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyApplyDefault.setVisibility(VISIBLE);
                        }
                    })
                    .start();

            int startCoordinateSingle = Dimensions.pxFromDp(54);
            mEnjoyApplyForOne.setTranslationY(startCoordinateSingle);
            mEnjoyApplyDefault.setAlpha(0);
            mEnjoyApplyForOne.animate().translationY(endCoordinate)
                    .alpha(1)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyApplyForOne.setVisibility(VISIBLE);
                        }
                    })
                    .start();

            int widthOfmThemeSetting = mEnjoyApplyBtn.getMeasuredWidth();
            float targetScaleX = 0.41f;
            mEnjoyApplyBtn.setPivotX(widthOfmThemeSetting);
            mEnjoyApplyBtn.setScaleX(1.0f);
            mEnjoyApplyBtn.setAlpha(1);
            mEnjoyApplyBtn.animate().scaleX(targetScaleX)
                    .alpha(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEnjoyApplyBtn.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting_click);
                        }
                    })
                    .start();
            mEnjoyClose.setTranslationX(-Dimensions.pxFromDp(21));
            mEnjoyClose.setRotation(90);
            mEnjoyClose.setAlpha(0f);
            mEnjoyClose.animate()
                    .alpha(1)
                    .rotation(180)
                    .translationX(0)
                    .setInterpolator(getmInterForTheme())
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyClose.setVisibility(VISIBLE);
                            mEnjoyClose.setBackgroundResource(R.drawable.shape_theme_setting_close);
                        }
                    })
                    .start();
            foldingOrNot = THEME_ENJOY_UNFOLDING;

        }

        private void foldView() {
            int endCoordinateDefault = Dimensions.pxFromDp(110);
            int startCoordinate = 0;
            mEnjoyApplyDefault.setTranslationY(startCoordinate);
            mEnjoyApplyDefault.setAlpha(1);
            mEnjoyApplyDefault.animate().translationY(endCoordinateDefault)
                    .alpha(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEnjoyApplyDefault.setVisibility(GONE);
                        }
                    })
                    .start();

            int endCoordinateSingle = Dimensions.pxFromDp(54);
            mEnjoyApplyForOne.setTranslationY(startCoordinate);
            mEnjoyApplyForOne.setAlpha(1);
            mEnjoyApplyForOne.animate().translationY(endCoordinateSingle)
                    .alpha(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEnjoyApplyForOne.setVisibility(GONE);
                        }
                    })
                    .start();

            int widthOfmThemeSetting = mEnjoyApplyBtn.getMeasuredWidth();
            float scaleX = 0.41f;
            mEnjoyApplyBtn.setPivotX(widthOfmThemeSetting);
            mEnjoyApplyBtn.setScaleX(scaleX);
            mEnjoyApplyBtn.setAlpha(0);
            mEnjoyApplyBtn.animate().scaleX(1.0f)
                    .alpha(1)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyApplyBtn.setVisibility(VISIBLE);
                            mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting);
                        }
                    })
                    .start();
            mEnjoyClose.setTranslationX(0f);
            mEnjoyClose.setRotation(180);
            mEnjoyClose.setAlpha(1f);
            mEnjoyClose.animate()
                    .alpha(0f)
                    .rotation(90)
                    .translationX(-Dimensions.pxFromDp(21))
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(getmInterForTheme())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEnjoyClose.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEnjoyClose.setBackgroundResource(R.drawable.shape_theme_setting_close_click);
                        }
                    })
                    .start();
            foldingOrNot = THEME_ENJOY_FOLDING;
        }
    }

    public static class DownloadTask {
        private static final int PENDING = 1;
        private static final int DOWNLOADING = 2;
        private static final int FINISH = 3;

        private static final int TYPE_THEME = 1;
        private static final int TYPE_RINGTONE = 2;

        TasksManagerModel mTasksManagerModel;

        public DownloadTask() {
        }

        public DownloadTask(TasksManagerModel tasksManagerModel, int type) {
            mTasksManagerModel = tasksManagerModel;
            mType = type;
        }

        /**
         * Download status
         */
        int mStatus;
        int mType;

        public boolean isRingtone() {
            return mType == TYPE_RINGTONE;
        }

        public boolean isMediaTheme() {
            return mType == TYPE_THEME;
        }

        public TasksManagerModel getTasksManagerModel() {
            return mTasksManagerModel;
        }

        public void setTasksManagerModel(TasksManagerModel tasksManagerModel) {
            mTasksManagerModel = tasksManagerModel;
        }

        public int getStatus() {
            return mStatus;
        }

        public void setStatus(int status) {
            mStatus = status;
        }
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                if (themeLoading) {

                    onThemeLoading();
                    intoDownloadingMode();
                }

            }

        }
    }

    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }
}
