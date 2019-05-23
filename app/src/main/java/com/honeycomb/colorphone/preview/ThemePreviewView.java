package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.VideoManager;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.PopularThemePreviewActivity;
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
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Locale;

import hugo.weaving.DebugLog;

import static com.honeycomb.colorphone.activity.ColorPhoneActivity.NOTIFICATION_ON_REWARDED;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_SELECT;

/**
 * Created by sundxing on 17/8/4.
 */

// TODO : clean Theme & Ringtone logic
public class ThemePreviewView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final String TAG = ThemePreviewWindow.class.getSimpleName();

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final int MSG_DOWNLOAD = 10;

    private static final boolean PLAY_ANIMITION = true;
    private static final boolean NO_ANIMITION = false;

    private static final long AUTO_HIDE_TIME = 15000; //15s
    private static final long ANIMATION_DURATION = 300;
    private static final long WINDOW_ANIM_DURATION = 400;
    private static final int TRANS_IN_DURATION = 400;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;

    private View mUserView;
    private View mCallName;
    private View mNumberName;

    private ThemePreviewActivity mActivity;
    private View mRootView;

    private ProgressViewHolder mProgressViewHolder;
    private RingtoneViewHolder mRingtoneViewHolder;
    private TextView mApplyButton;
    private View mApplyForOne;
    private View mActionLayout;

    private View mNavBack;

    private ImageView previewImage;
    private Theme mTheme;
    private Type mThemeType;
    private View dimCover;

    private View mLockLayout; //may be null
    private ViewGroup mUnLockButton;
    private RewardVideoView mRewardVideoView;


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
    private ValueAnimator transAnimator;


    private int mPosition = -1;
    private int mPageSelectedPos = -1;
    /**
     * Play no Transition animation when page scroll.
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
                    showNavView(false);
                    for (ThemePreviewView preV : mActivity.getViews()) {
                        if (preV == ThemePreviewView.this) {
                            fadeOutActionView();
                        } else {
                            preV.fadeOutActionViewImmediately();
                        }
                    }
                    return true;

                case MSG_SHOW:
                    showNavView(true);
                    for (ThemePreviewView preV : mActivity.getViews()) {
                        if (preV == ThemePreviewView.this) {
                            fadeInActionView();
                        } else {
                            preV.fadeInActionViewImmediately();
                        }
                    }
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

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            if (triggerMediaReady()) {
                playDownloadOkTransAnimation();
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
            if (triggerMediaReady()) {
                playDownloadOkTransAnimation();
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

    public void setCustomStyle() {
        TextView name = (TextView) findViewById(R.id.first_line);
        ImageView avatar = (ImageView) findViewById(R.id.caller_avatar);
        avatar.setImageDrawable(ContextCompat.getDrawable(mActivity, mTheme.getAvatar()));
        name.setText(mTheme.getAvatarName());
    }

    public boolean isRewardVideoLoading() {
        if (mRewardVideoView != null && mRewardVideoView.isLoading()) {
            return true;
        }
        return false;
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
                    boolean isInHide = getTransBottomLayout().getTranslationY() == bottomBtnTransY;
                    if (isInHide) {
                        mHandler.sendEmptyMessage(MSG_SHOW);
                    }
                    boolean isShown = getTransBottomLayout().getTranslationY() == 0 && themeReady;
                    if (isShown) {
                        mHandler.sendEmptyMessage(MSG_HIDE);
                    } else {
                        scheduleNextHide();
                    }
                }
            }
        });
        callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        callActionView.setTheme(mThemeType);
        callActionView.setAutoRun(false);
        mApplyButton = (TextView) findViewById(R.id.theme_apply_btn);
        mActionLayout = findViewById(R.id.theme_apply_layout);


        mApplyForOne = findViewById(R.id.theme_set_for_one);
        mApplyForOne.setEnabled(mTheme.getId() != Theme.RANDOM_THEME);


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

        mApplyButton.setOnClickListener(new View.OnClickListener() {
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
                } else {
                    showNavView(false);
                    fadeOutActionView();
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

        mApplyForOne.setOnClickListener(new OnClickListener() {
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

    private View getTransBottomLayout() {
        return mActionLayout;
    }

    private boolean isDimming() {
        return dimCover != null && dimCover.getVisibility() == VISIBLE;
    }

    private void playDownloadOkTransAnimation() {
        mProgressViewHolder.fadeOut();
        dimCover.animate().alpha(0).setDuration(200);
        getTransBottomLayout().setVisibility(View.VISIBLE);
        animationDelay = 0;
        onThemeReady(NO_ANIMITION);
    }

    private boolean triggerMediaReady() {
        if (mWaitMediaReadyCount > 2 || mWaitMediaReadyCount <= 0) {
            throw new IllegalStateException("triggerMedia count invalid : " + mWaitMediaReadyCount);
        }
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
        dimCover.setVisibility(View.INVISIBLE);
        mProgressViewHolder.hide();

        // Video has audio
        if (mTheme.hasRingtone()) {
            mRingtoneViewHolder.setEnable(true);
            mRingtoneViewHolder.play();
        } else {
            mRingtoneViewHolder.setEnable(false);
            mRingtoneViewHolder.hide();
        }

        previewWindow.updateThemeLayout(mThemeType);

        if (mTheme.isLocked()) {
            switchToLockState();
        }

        setCustomStyle();

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

    @DebugLog
    private void onThemeApply() {
        saveThemeApplys(mTheme.getId());
        ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, mTheme.getId());
        // notify
        HSBundle bundle = new HSBundle();
        bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
        HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);

        setButtonState(true);
        for (ThemePreviewView preV : mActivity.getViews()) {
            int viewPos = (int) preV.getTag();
            if (viewPos != mPageSelectedPos) {
                preV.updateButtonState();
            }
        }

        Utils.showToast(mActivity.getString(R.string.apply_success));
        GuideSetDefaultActivity.start(mActivity, false);

        // Ringtone enabled
        if (mTheme.hasRingtone()) {
            if (mRingtoneViewHolder.isSelect()) {
                RingtoneHelper.setDefaultRingtoneInBackground(mTheme);
            } else {
                RingtoneHelper.resetDefaultRingtone();
            }
            Ap.Ringtone.onApply(mTheme);
        }

        if (!TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Apply", mTheme.getIdName());
            Analytics.logEvent(event,
                    "RingtoneState", mRingtoneViewHolder.isSelect() ? "On" : "Off");
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
                                scheduleNextHide();
                            }
                        }).start();
                    }
                });
                return true;
            }
        }
        return false;
    }

    private void switchToLockState() {
        ViewStub stub = findViewById(R.id.lock_layout);
        dimCover.setVisibility(INVISIBLE);
        mRingtoneViewHolder.hide();
        if (mLockLayout == null) {
            mLockLayout = stub.inflate();
            mUnLockButton = mLockLayout.findViewById(R.id.unlock_button_container);

            mUnLockButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRewardVideoToUnlockTheme();
                    Analytics.logEvent("Colorphone_Theme_Unlock_Clicked", "from", "detail_page", "themeName", mTheme.getName());
                }
            });
        }
        mActionLayout.setVisibility(INVISIBLE);
    }

    private void hideLock() {
        if (mLockLayout != null) {
            mLockLayout.setVisibility(GONE);
        }
    }


    private void playTransInAnimation(final Runnable completeRunnable) {
        mCallName = findViewById(R.id.first_line);
        mNumberName = findViewById(R.id.second_line);

        mUserView = findViewById(R.id.caller_avatar_container);
        if (mUserView == null) {
            mUserView = findViewById(R.id.caller_avatar);
        }

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

        mUserView.setVisibility(VISIBLE);
        mCallName.setVisibility(VISIBLE);
        mNumberName.setVisibility(VISIBLE);
        callActionView.setVisibility(VISIBLE);
        mActionLayout.setVisibility(VISIBLE);

        if (mNoTransition) {
            if (completeRunnable != null) {
                completeRunnable.run();
            }
            return;
        }
        int pHeight = Utils.getPhoneHeight(mActivity);



        final View[] animViews = new View[] {mUserView, mCallName, mNumberName, callActionView};
        final int[] alpha = new int[] {0, 0, 0 ,0};
        final float[] transY = new float[] {-pHeight * 0.15f, -pHeight * 0.12f, -pHeight * 0.12f, pHeight * 0.15f};
        final TimeInterpolator transInterpolator = new DecelerateInterpolator(2f);
        int duration = 300;
        for (int i = 0; i < animViews.length; i++) {
            View v = animViews[i];
            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            v.setAlpha(alpha[i]);
            v.setTranslationY(transY[i]);
        }

        transAnimator = ValueAnimator.ofFloat(0f, 1f);
        transAnimator.setDuration(duration);
        transAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float transFraction = transInterpolator.getInterpolation(fraction);
                HSLog.d("SUNDXING", "fraction = " + fraction + "transFraction = " + transFraction);
                for (int i = 0; i < animViews.length; i++) {
                    View v = animViews[i];
                    v.setAlpha(fraction);
                    v.setTranslationY(transY[i] * (1f - transFraction));
                }
            }
        });

        transAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (completeRunnable != null) {
                    completeRunnable.run();
                }
                for (View v : animViews) {
                    v.setAlpha(1.0f);
                    v.setTranslationY(0f);
                    v.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
        });
        transAnimator.setStartDelay(WINDOW_ANIM_DURATION);
        transAnimator.start();

    }

//    private View findViewById(int id) {
//        return mRootView.findViewById(id);
//    }

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
                fadeInActionView();
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
            showRingtoneSetButton();
            mWaitContactResult = false;
            return;
        }

        if (anim) {
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
                    }).setStartDelay(animationDelay).start();
        } else {
            if (themeReady) {
                onActionButtonReady();
            }
        }
        mRingtoneViewHolder.transIn(true, anim);
    }

    private void showRingtoneSetButton() {
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
        getTransBottomLayout().setTranslationY(0);
        mRingtoneViewHolder.transIn(true, false);
        animationDelay = 0;
        if (isSelectedPos() && !mTheme.isLocked()) {
            checkNewFeatureGuideView();
        } else {
            scheduleNextHide();
        }
    }

    private void scheduleNextHide() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, AUTO_HIDE_TIME);
    }

    public void onStart() {
        // We do not play animation if activity restart.
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
        boolean themeLoading = false;
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
        }

        // Show background if gif drawable not ready.
        if (mTheme != null) {
            if (!mThemeType.isMedia()){
                previewImage.setImageDrawable(null);
                previewImage.setBackgroundColor(Color.BLACK);
            } else {
                boolean overrideSize = ColorPhoneApplication.mWidth > IMAGE_WIDTH;

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

            if (mTheme.isLocked()) {
                switchToLockState();
            } else {
                hideLock();
            }
        }
    }

    public void onStop() {
        hasStopped = true;
        pauseAnimation();

        getTransBottomLayout().animate().cancel();
        mHandler.removeCallbacksAndMessages(null);
        if (transAnimator != null && transAnimator.isStarted()) {
            transAnimator.end();

            transAnimator.removeAllUpdateListeners();
            transAnimator.removeAllListeners();
        }

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
        if (themeReady) {
            resumed = true;
            previewWindow.playAnimation(mThemeType);
            callActionView.doAnimation();
        }

        if (mTheme != null && !TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Show", mTheme.getIdName());
            Analytics.logEvent(event);
        }
        Ap.Ringtone.onShow(mTheme);
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
        previewWindow.updateThemeLayout(mThemeType);
        setCustomStyle();


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
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onDetachedFromWindow");
        }
        onStop();

        if (mRewardVideoView != null) {
            mRewardVideoView.onCancel();
        }
        super.onDetachedFromWindow();
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
        if ((isSelectedPos() && mDownloadTasks != null)) {
            for (int i = 0; i < mDownloadTasks.size(); i++) {
                DownloadTask downloadTask = mDownloadTasks.valueAt(i);
                if (downloadTask != null && downloadTask.getStatus() == DownloadTask.PENDING) {
                    download(downloadTask);
                }
            }
        }
        triggerPageChangeWhenIdle = true;

        if (isSelectedPos() && mTheme.isLocked()) {
            Analytics.logEvent("Colorphone_Theme_Button_Unlock_show", "themeName", mTheme.getName());
        }
    }

    public void updateButtonState() {
        if (themeReady && navIsShow()) {
            setButtonState(isCurrentTheme());
        }
    }

    private void showRewardVideoToUnlockTheme() {
        mUnLockButton.setClickable(false);
        if (mRewardVideoView == null) {
            mRewardVideoView = new RewardVideoView((ViewGroup) findViewById(R.id.root), new RewardVideoView.OnRewarded() {
                @Override
                public void onRewarded() {
                    HSBundle bundle = new HSBundle();
                    bundle.putInt(ThemePreviewActivity.NOTIFY_THEME_KEY, mTheme.getId());
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_ON_REWARDED, bundle);
                    mTheme.setLocked(false);
                    hideLock();
                    Analytics.logEvent("Colorphone_Theme_Unlock_Success", "from", "detail_page", "themeName", mTheme.getName());
                }

                @Override
                public void onAdClose() {
                    mUnLockButton.setClickable(true);
                }

                @Override
                public void onAdCloseAndRewarded() {

                }

                @Override
                public void onAdShow() {
                    Analytics.logEvent("Colorphone_Rewardvideo_show", "from", "detail_page", "themeName", mTheme.getName());
                }

                @Override
                public void onAdFailed() {
                    mUnLockButton.setClickable(true);
                }
            }, true);
        }


        mRewardVideoView.onRequestRewardVideo();
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

            Drawable btnBg = BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.white_87_transparent),
                    getResources().getColor(R.color.black_20_transparent),
                    Dimensions.pxFromDp(24),false, true);

            ringtoneSetLayout = findViewById(R.id.ringtone_apply_layout);
            ringtoneSetLayout.setVisibility(GONE);
            transYTop = getResources().getDimension(R.dimen.ringtone_apply_layout_height);

            ringtoneChangeBtn = findViewById(R.id.ringtone_apply_change);
            ringtoneChangeBtn.setOnClickListener(this);
            ringtoneChangeBtn.setBackground(btnBg);

            ringtoneKeepBtn = findViewById(R.id.ringtone_apply_keep);
            ringtoneKeepBtn.setOnClickListener(this);
            ringtoneKeepBtn.setBackground(btnBg.mutate());
        }

        public void showRingtoneSettings() {
            dimCover.setVisibility(VISIBLE);
            dimCover.setAlpha(0);
            dimCover.animate().alpha(1).setDuration(200);

            ringtoneSetLayout.setVisibility(VISIBLE);
            ringtoneSetLayout.setAlpha(1);
            ringtoneChangeBtn.setTranslationY(transYTop);
            ringtoneKeepBtn.setTranslationY(transYTop);
            ringtoneChangeBtn.animate().setDuration(320).setInterpolator(mPathInterpolator).translationY(0).start();
            ringtoneKeepBtn.animate().setDuration(320).setInterpolator(mPathInterpolator).translationY(0).setStartDelay(40).start();
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
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Ringtone_Clicked", mTheme.getIdName());
            Analytics.logEvent(event,
                    "Type", currentSelect ? "TurnOff" : "TurnOn");

            if (currentSelect) {
                mute();
            } else {
                play();
            }
        }

        private void hide() {
            imageView.setVisibility(GONE);
        }

        private void mute() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(false);
            VideoManager.get().mute(true);
        }

        private boolean isSelect() {
            return imageView.isActivated();
        }

        private void play() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(true);
            VideoManager.get().mute(false);
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
                    hideRingtoneSettings();
                    if (mApplyForAll) {
                        onThemeApply();
                    } else {
                        ThemeSetHelper.onConfirm(ThemeSetHelper.getCacheContactList(), mTheme, null);
                        setAsRingtone(true, false);
                        Utils.showToast(mActivity.getString(R.string.apply_success));
                    }
                    break;
                case R.id.ringtone_apply_keep:
                    hideRingtoneSettings();
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

}
