package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.VideoManager;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
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
import static com.honeycomb.colorphone.preview.ThemeStateManager.ENJOY_MODE;
import static com.honeycomb.colorphone.preview.ThemeStateManager.PREVIEW_MODE;



/**
 * Created by sundxing on 17/8/4.
 */

// TODO : clean Theme & Ringtone logic
public class ThemePreviewView extends FrameLayout implements ViewPager.OnPageChangeListener, INotificationObserver {

    private static final String TAG = ThemePreviewWindow.class.getSimpleName();
    private static final String PREF_KEY_SCROLL_GUIDE_SHOWN = "pref_key_scroll_guide_shown";

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final int MSG_DOWNLOAD = 10;

    private static final boolean PLAY_ANIMITION = true;
    private static final boolean NO_ANIMITION = false;

    private static final long AUTO_HIDE_TIME = 15000; //15s
    private static final long ANIMATION_DURATION = 300;
    private static final long ANIMATION_DURMATION_DELAY = 1000;
    private static final long CHANGE_MODE_DURTION = 200;
    private static final long WINDOW_ANIM_DURATION = 400;
    private static final int TRANS_IN_DURATION = 400;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;

    private View mUserView;
    private TextView mCallName;
    private View mNumberName;

    private ThemePreviewActivity mActivity;
    private View mRootView;

    private ProgressViewHolder mProgressViewHolder;
    private RingtoneViewHolder mRingtoneViewHolder;
    private TextView mApplyButton;
    private View mApplyForOne;
    private View mActionLayout;
    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter intentFilter;
    private boolean themeLoading = false;


    private View mNavBack;

    private ImageView previewImage;
    private Theme mTheme;
    private Type mThemeType;
    private View dimCover;

    private View mLockLayout; //may be null
    private ViewGroup mUnLockButton;
    private RewardVideoView mRewardVideoView;

    private ThemeStateManager themeStateManager;

    private static final int THEME_ENJOY_UNFOLDING = 0;
    private static final int THEME_ENJOY_FOLDING = 1;
    public static final int NAV_FADE_IN = 1;
    private static final int NAV_VISIBLE = 0;
    private TextView mThemeLikeCount;
    private TextView mThemeTitle;
    private PercentRelativeLayout rootView;

    public static boolean ifShowThemeApplyView = false;
    public static boolean isSelected = false;
    private TextView mEnjoyApplyBtn;
    private TextView mEnjoyApplyDefault;
    private TextView mEnjoyApplyForOne;
    private ImageView mEnjoyClose;
    private LottieAnimationView mThemeLikeAnim;

    private int foldingOrNot = THEME_ENJOY_FOLDING;
    public static int navFadeInOrVisible = NAV_VISIBLE;
    private RelativeLayout mEnjoyThemeLayout;
    private TextView mThemeSelected;

    // DownloadTask
    private SparseArray<DownloadTask> mDownloadTasks = new SparseArray<>(2);

    /**
     * If button is playing animation
     */
    private boolean inTransition;
    private boolean themeReady;

    private long animationDelay = 500;
    private float bottomBtnTransY;
    private Interpolator mInter;


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

    private long startDownloadTime;

    private int mWaitMediaReadyCount = 0;
    private ProgressHelper mProgressHelper = new ProgressHelper();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE:
                    switchMode(PREVIEW_MODE);
                    themeStateManager.sendNotification(PREVIEW_MODE);
                return true;

                case MSG_SHOW:
                    switchMode(ENJOY_MODE);
                    themeStateManager.sendNotification(ENJOY_MODE);
                    return true;

                case MSG_DOWNLOAD: {
                    final int type = msg.arg1;
                    DownloadTask task = mDownloadTasks.get(type);
                    if (task != null) {
                        if (isSelectedPos()) {
                            download(task);
                        } else {
                            task.setStatus(DownloadTask.PENDING);
                        }
                    }
                    return true;
                }
                default:
                    return false;

            }
        }
    });

    private StateChangeObserver observer = new StateChangeObserver() {
        @Override
        public void onReceive(int themeMode) {

            switchMode(themeMode);
        }
    };

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            clearTask(DownloadTask.TYPE_THEME);

            if (triggerMediaReady()) {
                playDownloadOkTransAnimation();
                onMediaDownloadOK();
            }
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
            clearTask(DownloadTask.TYPE_RINGTONE);
            if (triggerMediaReady()) {
                playDownloadOkTransAnimation();
                onMediaDownloadOK();
            }
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
            // Do nothing
            if (BuildConfig.DEBUG) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("Ringtone", "Download failed : " +  mTheme.getIdName() + ", progress: " + (int) (percent * 100));
            }
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
            final float percent = sofar
                    / (float) total;

            mProgressHelper.setProgressRingtone((int) (percent * 100));
            mProgressViewHolder.updateProgressView(mProgressHelper.getRealProgress());
            if (BuildConfig.DEBUG) {
                HSLog.d("Ringtone", "Downloading : " +  mTheme.getIdName() + ", progress: " + (int) (percent * 100));
            }
        }
    };

    Runnable transEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mBlockAnimationForPageChange) {
                resumeAnimation();
                mBlockAnimationForPageChange = true;
            }

            boolean curTheme = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
            animationDelay = 0;
            setButtonState(curTheme);
            playButtonAnimation();

        }
    };
    private boolean mWaitContactResult;
    private boolean mWaitForAll;
    private boolean mWindowInTransition;
    private boolean mPendingResume;

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

    public void init(ThemePreviewActivity activity, ArrayList<Theme> themes, int position, View navBack) {
        mActivity = activity;
        mTheme = themes.get(position);
        mPosition = position;
        mNavBack = navBack;
//        mRootView = null;
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

        mCallName = findViewById(R.id.first_line);
        mCallName.setText(mTheme.getAvatarName());

        mNumberName = findViewById(R.id.second_line);

        ImageView avatar = (ImageView) findViewById(R.id.caller_avatar);
        avatar.setImageDrawable(ContextCompat.getDrawable(mActivity, mTheme.getAvatar()));
        mUserView = findViewById(R.id.caller_avatar_container);
        if (mUserView == null) {
            mUserView = avatar;
        }
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
        mHandler.sendEmptyMessage(MSG_SHOW);
        mEnjoyApplyBtn.setVisibility(VISIBLE);
        mRingtoneViewHolder.hideRingtoneSettings();
    }

    public void stopRewardVideoLoading() {
        if (mRewardVideoView != null) {
            mRewardVideoView.onHideAdLoading();
            mRewardVideoView.onCancel();
            mUnLockButton.setClickable(true);
        }
    }

    protected void onCreate() {
        previewWindow = (ThemePreviewWindow) findViewById(R.id.card_flash_preview_window);
        previewWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (themeReady
                        && !inTransition
                        && !mRingtoneViewHolder.isRingtoneSettingsShow()) {
                    if (getThemeMode() == PREVIEW_MODE) {
                        mHandler.sendEmptyMessage(MSG_SHOW);

                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        if (foldingOrNot == THEME_ENJOY_UNFOLDING) {
                            foldView();
                        } else {
                            mHandler.sendEmptyMessage(MSG_HIDE);
                        }
                    }
                }
                if (mRingtoneViewHolder.isRingtoneSettingsShow()) {
                    dismissRingtoneSettingPage();
                }
            }
        });
        previewWindow.setVisibility(INVISIBLE);
        themeStateManager = ThemeStateManager.getInstance();
        callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        callActionView.setTheme(mThemeType);
        callActionView.setAutoRun(false);
        mApplyButton = (TextView) findViewById(R.id.theme_apply_btn);
        mActionLayout = findViewById(R.id.theme_apply_layout);


        mApplyForOne = findViewById(R.id.theme_set_for_one);
        mApplyForOne.setEnabled(mTheme.getId() != Theme.RANDOM_THEME);

        mEnjoyThemeLayout = findViewById(R.id.enjoy_layout);
        mThemeTitle = findViewById(R.id.title_line);
        mThemeTitle.setText(mTheme.getName());
        mThemeLikeCount = findViewById(R.id.collect_num);
        mEnjoyApplyBtn = findViewById(R.id.theme_setting);
        mEnjoyApplyDefault = findViewById(R.id.theme_setting_default);
        mEnjoyApplyForOne = findViewById(R.id.theme_setting_single);
        mEnjoyClose = findViewById(R.id.theme_setting_close);
        mThemeLikeAnim = findViewById(R.id.like_count_icon);
        mThemeSelected = findViewById(R.id.theme_selected);
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
                    mHandler.sendEmptyMessage(MSG_SHOW);
                    mEnjoyApplyBtn.setVisibility(VISIBLE);
                } else {
                    showNavView(false);
                    fadeOutActionView();
                    navFadeInOrVisible = NAV_FADE_IN;
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
        bottomBtnTransY = getTransBottomLayout().getTranslationY();

        mInter = new AccelerateDecelerateInterpolator();

    }

    private void onApplyForAll() {
        if (!mTheme.hasRingtone()) {
            onThemeApply();
        } else {
            showNavView(false);
            fadeOutActionView();
            navFadeInOrVisible = NAV_FADE_IN;
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

    private View getTransBottomLayout() {
        return mActionLayout;
    }

    private void playDownloadOkTransAnimation() {
        mProgressViewHolder.fadeOut();
        dimCover.animate().alpha(0).setDuration(200);
        animationDelay = 0;
    }

    private void onMediaDownloadOK() {
        onThemeReady(NO_ANIMITION);
        checkCheckGuideView();
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
     * @param needTransAnim
     */
    private void onThemeReady(boolean needTransAnim) {
        themeReady = true;
        themeLoading = false;

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

        updateThemePreviewLayout(mThemeType);

        switchMode(getThemeMode(), needTransAnim);


        if (needTransAnim || mCallName.getVisibility() != VISIBLE) {
            playTransInAnimation(transEndRunnable);
        } else {
            transEndRunnable.run();
        }

        if (startDownloadTime != 0) {
            Analytics.logEvent("ColorPhone_Theme_Download_Time", "Time",
                    String.valueOf((System.currentTimeMillis() - startDownloadTime + 999) / DateUtils.SECOND_IN_MILLIS));
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

        Utils.showApplyView(rootView, mNavBack);
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


    private boolean checkNewFeatureGuideView() {
        //TODO remove guide if no need to show.
        if (mTheme.getId() == Theme.RANDOM_THEME) {
            if (ModuleUtils.needShowRandomThemeGuide()) {
                ViewStub stub = findViewById(R.id.guide_for_random_theme);
                final View guideView = stub.inflate();
                guideView.setAlpha(0);
                guideView.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
                View buttonOk = guideView.findViewById(R.id.guide_random_ok);
                buttonOk.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, (float)Dimensions.pxFromDp(25), false)
                );
                buttonOk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        guideView.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                guideView.setOnClickListener(null);
                                guideView.setVisibility(GONE);
                            }
                        }).start();
                    }
                });
                return true;
            }
        }
        return false;
    }

    private boolean checkCheckGuideView() {
        if (Preferences.getDefault().getBoolean(PREF_KEY_SCROLL_GUIDE_SHOWN, true)) {
            ViewStub stub = findViewById(R.id.preview_guide_viewstub);
            final View guideView = stub.inflate();
            guideView.setAlpha(0);
            guideView.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
            guideView.setOnClickListener(v -> {
                mActivity.findViewById(R.id.nav_back).setAlpha(1f);
                guideView.animate().alpha(0).translationY(-Dimensions.getPhoneHeight(getContext())).setDuration(ANIMATION_DURATION)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                guideView.setVisibility(GONE);
                            }
                        }).start();
                Preferences.getDefault().putBoolean(PREF_KEY_SCROLL_GUIDE_SHOWN, false);
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
        switch (mode) {
            case ENJOY_MODE:
                foldingOrNot = THEME_ENJOY_FOLDING;
                if (anim) {
                    setEnjoyView();
                } else {
                    intoEnjoyView();
                }
                break;
            case PREVIEW_MODE:
                changeModeToPreview();
                break;
            default:
                break;
        }
    }

    public void switchMode(int mode) {
        switchMode(mode, true);
    }

    private void setModeVisible(int mode, boolean visible) {
        int visibleValue = visible ? VISIBLE : GONE;
        switch (mode) {
            case ENJOY_MODE:
                mEnjoyThemeLayout.setVisibility(visibleValue);
                break;
            case PREVIEW_MODE:
                callActionView.setVisibility(visibleValue);
                mUserView.setVisibility(visibleValue);
                mNumberName.setVisibility(visibleValue);
                mCallName.setVisibility(visibleValue);
                mActionLayout.setVisibility(visibleValue);
                break;
            default:
                break;
        }
    }

    private void intoDownloadingMode() {
        mEnjoyThemeLayout.setVisibility(GONE);
        callActionView.setVisibility(GONE);
        mUserView.setVisibility(GONE);
        mNumberName.setVisibility(GONE);
        mCallName.setVisibility(GONE);
        mActionLayout.setVisibility(GONE);
        mEnjoyApplyBtn.setVisibility(GONE);
        mNavBack.setVisibility(VISIBLE);
        mEnjoyClose.setVisibility(GONE);
        mEnjoyApplyDefault.setVisibility(GONE);
        mEnjoyApplyForOne.setVisibility(GONE);
    }

    private void intoEnjoyView() {
        mEnjoyThemeLayout.setVisibility(VISIBLE);

        callActionView.setVisibility(GONE);
        mUserView.setVisibility(GONE);
        mNumberName.setVisibility(GONE);
        mCallName.setVisibility(GONE);
        mActionLayout.setVisibility(GONE);
        mEnjoyApplyBtn.setVisibility(VISIBLE);

        mNavBack.setVisibility(VISIBLE);
        mEnjoyClose.setVisibility(GONE);
        mEnjoyApplyDefault.setVisibility(GONE);
        mEnjoyApplyForOne.setVisibility(GONE);
        mEnjoyApplyBtn.setScaleX(1.0f);
        mEnjoyApplyBtn.setAlpha(1);
        mEnjoyApplyBtn.setTextColor(Color.WHITE);
        mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting);

        mEnjoyApplyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.logEvent("ColorPhone_FullScreen_SetAsFlash_Clicked");
                unFoldView();
            }
        });

        mEnjoyClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                foldView();
            }
        });
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

    private void setEnjoyView() {
         mActionLayout.setVisibility(GONE);

         mThemeLikeCount.setText(String.valueOf(mTheme.getDownload()));
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


         if (navFadeInOrVisible == NAV_FADE_IN) {
             showNavView(true);
             fadeInActionView();
             navFadeInOrVisible = NAV_VISIBLE;
         } else {
             mNavBack.setVisibility(VISIBLE);
         }
         if (ifThemeSelected()) {
             mThemeSelected.setVisibility(VISIBLE);
         } else {
             mThemeSelected.setVisibility(GONE);
         }
         changeModeToEnjoy();
         mEnjoyClose.setVisibility(GONE);
         mEnjoyApplyDefault.setVisibility(GONE);
         mEnjoyApplyForOne.setVisibility(GONE);
         mEnjoyApplyBtn.setScaleX(1.0f);
         mEnjoyApplyBtn.setAlpha(1);
         mEnjoyApplyBtn.setTextColor(Color.WHITE);
         mEnjoyApplyBtn.setBackgroundResource(R.drawable.shape_theme_setting);

         mEnjoyApplyBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {

                 unFoldView();
             }
         });

         mEnjoyClose.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 foldView();
             }
         });
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

    private void changeModeToPreview() {
        mEnjoyThemeLayout.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mEnjoyThemeLayout.setVisibility(GONE);
                    }
                })
                .start();
        mUserView.setAlpha(0);
        mUserView.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mUserView.setVisibility(VISIBLE);
                    }
                })
                .start();
        mNumberName.setAlpha(0);
        mNumberName.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mNumberName.setVisibility(VISIBLE);
                    }
                })
                .start();
        callActionView.setAlpha(0);
        callActionView.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        callActionView.setVisibility(VISIBLE);
                    }
                })
                .start();
        mCallName.setAlpha(0);
        mCallName.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mCallName.setVisibility(VISIBLE);
                    }
                })
                .start();
        if (getThemeMode() == PREVIEW_MODE) {
            mActionLayoutfadeInView();
        }

    }
    private void changeModeToEnjoy() {
        mEnjoyThemeLayout.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mEnjoyThemeLayout.setVisibility(VISIBLE);
                    }
                })
                .start();
        mEnjoyApplyBtn.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mEnjoyApplyBtn.setVisibility(VISIBLE);
                    }
                })
                .start();
        callActionView.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        callActionView.setVisibility(GONE);
                    }
                })
                .start();
        mUserView.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mUserView.setVisibility(GONE);
                    }
                })
                .start();
        mCallName.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCallName.setVisibility(GONE);
                    }
                })
                .start();
        mNumberName.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mNumberName.setVisibility(GONE);
                    }
                })
                .start();
    }

    private Interpolator getmInterForTheme() {
        Interpolator mInterForTheme = PathInterpolatorCompat.create(0.175f, 0.885f, 0.32f, 1.275f);
        return mInterForTheme;
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

    public boolean isThemeSettingShow() {
        return foldingOrNot == THEME_ENJOY_UNFOLDING;
    }

    public void returnThemeSettingPage() {
        foldView();
    }

    private void mActionLayoutfadeInView() {
        int mActionLayoutHeight = Dimensions.pxFromDp(60);
        mActionLayout.setTranslationY(mActionLayoutHeight);
        getTransBottomLayout().animate().translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(mInter)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (getThemeMode() == PREVIEW_MODE) {
                            onActionButtonReady();
                            inTransition = false;
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getThemeMode() == PREVIEW_MODE) {
                            mActionLayout.setVisibility(VISIBLE);
                        }
                    }
                }).setStartDelay(ANIMATION_DURMATION_DELAY).start();
    }

    private void playTransInAnimation(final Runnable completeRunnable) {
        if (mTheme.isLocked()) {
            mCallName.setVisibility(INVISIBLE);
            mNumberName.setVisibility(INVISIBLE);
            mUserView.setVisibility(INVISIBLE);
            callActionView.setVisibility(INVISIBLE);

            if (completeRunnable != null) {
                completeRunnable.run();
            }
            return;
        }

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

    private void playButtonAnimation() {
        if (navIsShow()) {
            if (mNoTransition) {
                fadeInActionViewImmediately();
            } else {
                //fadeInActionView();
                if (needShowRingtoneSetButton()) {
                    showNavView(false);
                    fadeOutActionView();
                    mRingtoneViewHolder.setApplyForAll(false);
                    navFadeInOrVisible = NAV_FADE_IN;
                    showRingtoneSetButton();
                    mWaitContactResult = false;
                }
            }
        } else {
            fadeOutActionViewImmediately();
        }
    }

    private boolean navIsShow() {
        return Math.abs(mNavBack.getTranslationX()) <= 1;
    }

    private CharSequence getString(int id) {
        return mActivity.getString(id);
    }

    private void showNavView(boolean show) {
        float offsetX = Dimensions.isRtl() ?  -Dimensions.pxFromDp(60) : Dimensions.pxFromDp(60);
        float targetX = show ? 0 : -offsetX;
        // State already right.
        if (Math.abs(mNavBack.getTranslationX() - targetX) <= 1) {
            return;
        }
        if (isSelectedPos()) {
            mNavBack.animate().translationX(targetX)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(mInter)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (show == true) {
                                mNavBack.setVisibility(VISIBLE);
                            }
                        }
                    })
                    .start();
        } else {
            mNavBack.setTranslationX(targetX);
        }
    }

    private void fadeInActionView() {
        fadeInActionView(true);
    }

    public void fadeInActionViewImmediately() {
        fadeInActionView(false);
    }

    private void fadeInActionView(boolean anim) {
        if (needShowRingtoneSetButton()) {
            showNavView(false);
            fadeOutActionView();
            mRingtoneViewHolder.setApplyForAll(false);
            navFadeInOrVisible = NAV_FADE_IN;
            showRingtoneSetButton();
            mWaitContactResult = false;
            return;
        }

        if (anim) {
            int mActionLayoutHeight = Dimensions.pxFromDp(60);
            mActionLayout.setTranslationY(mActionLayoutHeight);
            inTransition = true;
            getTransBottomLayout().animate().translationY(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(mInter)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            onActionButtonReady();
                            inTransition = false;

                        }
                    }).setStartDelay(ANIMATION_DURMATION_DELAY).start();
        } else {
            if (themeReady) {
                onActionButtonReady();
            }
        }
        mRingtoneViewHolder.transIn(true, anim);
    }

    private void showRingtoneSetButton() {
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

    private void fadeOutActionView(boolean anim) {
        if (anim) {
            inTransition = true;
            getTransBottomLayout().animate().translationY(bottomBtnTransY).setDuration(
                    isSelectedPos() ? ANIMATION_DURATION : 0).setInterpolator(mInter).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    getTransBottomLayout().setTranslationY(bottomBtnTransY);
                    mRingtoneViewHolder.transIn(false, false);
                    inTransition = false;
                }
            }).setStartDelay(0).start();
        } else {
            getTransBottomLayout().setTranslationY(bottomBtnTransY);
        }
        mRingtoneViewHolder.transIn(false, isSelectedPos());
    }

    private void fadeOutActionView() {
        fadeOutActionView(true);
    }

    public void fadeOutActionViewImmediately() {
        fadeOutActionView(false);
    }

    public void onActionButtonReady() {
        mRingtoneViewHolder.transIn(true, false);
        animationDelay = 0;
        if (isSelectedPos() && !mTheme.isLocked()) {
            checkNewFeatureGuideView();
        }
    }

    private boolean ifThemeSelected () {
        return ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
    }

    public void onStart() {

        mWaitMediaReadyCount = 0;
        // We do not play animation if activity restart.
        // TODO as method
        boolean playTrans = !hasStopped;
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
                if (ifShowThemeApplyView) {
                    Utils.showApplyView(rootView, mNavBack);
                    ifShowThemeApplyView = false;
                }

                onVideoReady(playTrans);
                checkCheckGuideView();
            } else {
                mDownloadTasks.put(DownloadTask.TYPE_THEME, new DownloadTask(model, DownloadTask.TYPE_THEME));
                themeLoading = true;
            }
        } else {
            // Directly applicable
            onThemeReady(playTrans);
        }

        if (hasRingtone)  {
            if (TasksManager.getImpl().isDownloaded(ringtoneModel)) {
                onRingtoneReady(playTrans);
            } else {
                // Ringtone data not ready yet. If theme data not loads, we load ringtone separately.
                mDownloadTasks.put(DownloadTask.TYPE_RINGTONE, new DownloadTask(ringtoneModel, DownloadTask.TYPE_RINGTONE));
                themeLoading = true;
            }
        }

        if (themeLoading) {
            startDownloadTime = System.currentTimeMillis();

            onThemeLoading();
            intoDownloadingMode();
        }

        // Show background if gif drawable not ready.
        if (mTheme != null) {
            if (!mThemeType.isMedia()){
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
                request.into(previewImage);

            }

        }
    }

    public void onStop() {

        hasStopped = true;
        pauseAnimation();

        getTransBottomLayout().animate().cancel();
        mHandler.removeCallbacksAndMessages(null);

        for (int i = 0; i < mDownloadTasks.size(); i++) {
            DownloadTask downloadTask = mDownloadTasks.valueAt(i);
            FileDownloadMultiListener.getDefault().removeStateListener(downloadTask.getTasksManagerModel().getId());
        }

    }

    private void pauseAnimation() {
        if (themeReady) {
            previewWindow.stopAnimations();
            callActionView.stopAnimations();
            resumed = false;
        }
    }

    private void resumeAnimation() {
        if (mWindowInTransition) {
            mPendingResume = true;
            return;
        }

        if (themeReady) {
            resumed = true;
            previewWindow.setVisibility(VISIBLE);
            previewWindow.playAnimation(mThemeType);
            callActionView.doAnimation();
            if (mTheme.hasRingtone()) {
                mRingtoneViewHolder.refreshMuteStatus();
            }
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
        updateThemePreviewLayout(mThemeType);

        mProgressViewHolder.mProgressTxtGroup.setAlpha(0);
        mProgressViewHolder.mProgressBar.setAlpha(0);
        playTransInAnimation(new Runnable() {
            @Override
            public void run() {
                int duration = TRANS_IN_DURATION;
                mProgressViewHolder.transIn(bottomBtnTransY, duration);

                final DownloadTask task = mDownloadTasks.get(DownloadTask.TYPE_THEME);
                if (task != null) {
                    float percent = TasksManager.getImpl().getDownloadProgress(task.getTasksManagerModel().getId());
                    mProgressHelper.setProgressVideo((int) (percent * 100));
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD;
                    msg.arg1 = DownloadTask.TYPE_THEME;
                    mHandler.sendMessageDelayed(msg, duration);
                }

                final DownloadTask ringtoneTask = mDownloadTasks.get((DownloadTask.TYPE_RINGTONE));
                if (ringtoneTask != null) {
                    float percent = TasksManager.getImpl().getDownloadProgress(ringtoneTask.getTasksManagerModel().getId());
                    mProgressHelper.setProgressRingtone((int) (percent * 100));
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD;
                    msg.arg1 = DownloadTask.TYPE_RINGTONE;
                    mHandler.sendMessageDelayed(msg, duration);
                }

                if (ringtoneTask != null || task != null) {
                    mProgressViewHolder.updateProgressView(mProgressHelper.getRealProgress());
                }
            }
        });
    }

    private void download(DownloadTask task) {
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
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();

        getContext().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unregisterForInternetChange() {

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
        onStart();
        HSGlobalNotificationCenter.addObserver(StartGuideActivity.NOTIFICATION_PERMISSION_GRANT, this);
        registerForInternetChange();
        themeStateManager.registerForThemeStateChange(observer);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onDetachedFromWindow");
        }
        unregisterForInternetChange();
        themeStateManager.unregisterForThemeStateChange(observer);
        onStop();

        if (mRewardVideoView != null) {
            mRewardVideoView.onCancel();
        }
        super.onDetachedFromWindow();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
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
            HSLog.d("onPageSelected " + position);
        }
        mPageSelectedPos = position;

        boolean isCurrentPageActive = isSelectedPos();

        if (isCurrentPageActive) {
            // Update download task
            if (mDownloadTasks != null){
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
        if (themeReady && navIsShow()) {
            setButtonState(isCurrentTheme());
        }
    }

    private void showRewardVideoToUnlockTheme() {
       
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
    }


    public void setNoTransition(boolean noTransition) {
        mNoTransition = noTransition;
    }

    public void setBlockAnimationForPageChange(boolean blockAnimationForPageChange) {
        if (isSelectedPos()) {
            mBlockAnimationForPageChange = blockAnimationForPageChange;
        }
    }

    public void onWindowTransitionStart() {
        mWindowInTransition = true;
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
        private ProgressBar mProgressBar;
        private TextView mProgressTxt;
        private View mProgressTxtGroup;

        public ProgressViewHolder() {
            mProgressBar = (ProgressBar) findViewById(R.id.theme_progress_bar);
            mProgressTxt = (TextView) findViewById(R.id.theme_progress_txt);
            mProgressTxtGroup= findViewById(R.id.theme_progress_txt_holder);
        }

        public void updateProgressView(int percent) {
            mProgressBar.setProgress(percent);
            mProgressTxt.setText(mActivity.getString(R.string.loading_progress, percent));
        }

        public void hide() {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressTxtGroup.setVisibility(View.INVISIBLE);
        }

        public void show() {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressTxtGroup.setVisibility(View.VISIBLE);
        }

        public void fadeOut() {
            mProgressBar.animate().alpha(0).setDuration(300).start();
            mProgressTxtGroup.animate().alpha(0).setDuration(300).start();
        }

        public void transIn(float bottomBtnTransY, int duration) {
            TimeInterpolator interp = new DecelerateInterpolator(1.5f);
            mProgressBar.setTranslationY(bottomBtnTransY);
            mProgressTxtGroup.setTranslationY(bottomBtnTransY);
            mProgressBar.animate().alpha(1).translationY(0).setDuration(duration).setInterpolator(interp).start();
            mProgressTxtGroup.animate().alpha(1).translationY(0).setDuration(duration).setInterpolator(interp).start();
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

            ringtoneSetLayout = findViewById(R.id.ringtone_apply_layout);
            ringtoneSetLayout.setVisibility(GONE);
            transYTop = getResources().getDimension(R.dimen.ringtone_apply_layout_height);

            ringtoneChangeBtn = findViewById(R.id.ringtone_apply_change);
            ringtoneChangeBtn.setOnClickListener(this);
            ringtoneChangeBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.white_87_transparent),
                    getResources().getColor(R.color.black_20_transparent),
                    Dimensions.pxFromDp(24),false, true));

            ringtoneKeepBtn = findViewById(R.id.ringtone_apply_keep);
            ringtoneKeepBtn.setOnClickListener(this);
            ringtoneKeepBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.white_87_transparent),
                    getResources().getColor(R.color.black_20_transparent),
                    Dimensions.pxFromDp(24),false, true));
        }

        public void showRingtoneSettings() {
            dimCover.setVisibility(VISIBLE);
            dimCover.setAlpha(0);
            dimCover.animate().alpha(1).setDuration(200);

            ringtoneSetLayout.setVisibility(VISIBLE);
            ringtoneSetLayout.setAlpha(1);
            ringtoneChangeBtn.setTranslationY(transYTop);
            ringtoneKeepBtn.setTranslationY(transYTop);
            ringtoneChangeBtn.animate().setDuration(240).setInterpolator(mPathInterpolator).translationY(0).start();
            ringtoneKeepBtn.animate().setDuration(240).setInterpolator(mPathInterpolator).translationY(0).setStartDelay(40).start();

            Analytics.logEvent("Ringtone_Set_Shown", "ThemeName", mTheme.getName());
        }

        public boolean isRingtoneSettingsShow() {
            return ringtoneSetLayout.getVisibility() == VISIBLE && ringtoneSetLayout.getAlpha() > 0;
        }

        public void hideRingtoneSettings() {
            dimCover.animate().alpha(0).setDuration(200);
            ringtoneSetLayout.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ringtoneSetLayout.setVisibility(GONE);
                    dimCover.setVisibility(INVISIBLE);

                }
            });
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
            float offsetX = Dimensions.isRtl() ?  -Dimensions.pxFromDp(60) : Dimensions.pxFromDp(60);
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
                        Utils.showApplyView(rootView, mNavBack);

                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        navFadeInOrVisible = NAV_FADE_IN;
                        mHandler.sendEmptyMessage(MSG_SHOW);
                        mEnjoyApplyBtn.setVisibility(VISIBLE);
                    } else {
                        navFadeInOrVisible = NAV_FADE_IN;
                        mHandler.sendEmptyMessage(MSG_HIDE);
                    }
                    break;
                case R.id.ringtone_apply_keep:
                    Analytics.logEvent("Ringtone_Default_Set_Success", "ThemeName", mTheme.getName());

                    hideRingtoneSettings();
                    if (mApplyForAll) {
                        onThemeApply();
                    } else {
                        ThemeSetHelper.onConfirm(ThemeSetHelper.getCacheContactList(), mTheme, null);
                        Utils.showApplyView(rootView, mNavBack);
                    }
                    if (getThemeMode() == ENJOY_MODE) {
                        navFadeInOrVisible = NAV_FADE_IN;
                        mHandler.sendEmptyMessage(MSG_SHOW);
                        mEnjoyApplyBtn.setVisibility(VISIBLE);
                    } else {
                        navFadeInOrVisible = NAV_FADE_IN;
                        mHandler.sendEmptyMessage(MSG_HIDE);
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

    public static class DownloadTask {
        private static final int PENDING = 1;
        private static final int DOWNLOADING = 2;
        private static final int FINISH =  3;

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

}
