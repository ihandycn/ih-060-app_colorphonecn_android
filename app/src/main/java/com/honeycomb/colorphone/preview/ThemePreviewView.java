package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.activity.PopularThemePreviewActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.view.RewardVideoView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

import java.io.IOException;
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

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final int MSG_DOWNLOAD = 10;
    private static final int MSG_RINGTONE_HELLO = 12;

    private static final boolean PLAY_ANIMITION = true;
    private static final boolean NO_ANIMITION = false;

    private static final long AUTO_HIDE_TIME = 15000; //15s
    private static final long ANIMATION_DURATION = 400;
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
    private Button mApplyButton;
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
    private OvershootInterpolator mInter;
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

    /**
     * If Ringtone file ready, theme file is downloading, wait.
     */
    private boolean waitingForThemeReady = false;
    private boolean resumed;

    private long startDownloadTime;

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
                case MSG_RINGTONE_HELLO :
                    mRingtoneViewHolder.hello();
                    return true;
                default:
                    return false;

            }
        }
    });

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            playDownloadOkTransAnimation();
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
            mProgressViewHolder.updateProgressView((int) (percent * 100));
        }
    };

    DownloadStateListener mRingtoneDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            onRingtoneReady();
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
            // Do nothing
            if (BuildConfig.DEBUG) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("Ringtone", "Downloading : " +  mTheme.getIdName() + ", progress: " + (int) (percent * 100));
            }
        }
    };


    Runnable transEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (waitingForThemeReady) {
                waitingForThemeReady  = false;
                onRingtoneReady();
            }
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

    public static void saveThemeApplys(int themeId) {
        // TODO MMKV
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
                if (themeReady && !inTransition) {
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
        mApplyButton = (Button) findViewById(R.id.theme_apply_btn);
        mApplyButton.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD));
        mActionLayout = findViewById(R.id.theme_apply_layout);
        mApplyForOne = findViewById(R.id.theme_set_for_one);

        if (mTheme.getId() == Theme.RANDOM_THEME) {
            mApplyForOne.setVisibility(GONE);
        }
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
                onThemeApply();

                if (mActivity instanceof PopularThemePreviewActivity) {
                    LauncherAnalytics.logEvent("Colorphone_BanboList_ThemeDetail_SetForAll");
                    LauncherAnalytics.logEvent("ColorPhone_BanboList_Set_Success");
                } else {
                    LauncherAnalytics.logEvent("Colorphone_MainView_ThemeDetail_SetForAll");
                    LauncherAnalytics.logEvent("ColorPhone_MainView_Set_Success");
                }
            }
        });

        mApplyForOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                    PermissionChecker.getInstance().check(mActivity, "SetForSomeone");
                }

                LauncherAnalytics.logEvent("Colorphone_SeletContactForTheme_Started", "ThemeName", mTheme.getIdName());
                if (mActivity instanceof PopularThemePreviewActivity) {
                    ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_POPULAR_THEME);
                    LauncherAnalytics.logEvent("Colorphone_BanboList_ThemeDetail_SeletContactForTheme_Started");
                } else {
                    LauncherAnalytics.logEvent("Colorphone_MainView_ThemeDetail_SeletContactForTheme_Started");
                    ContactsActivity.startSelect(mActivity, mTheme, ContactsActivity.FROM_TYPE_MAIN);
                }
            }
        });
        bottomBtnTransY = getTransBottomLayout().getTranslationY();

        mInter = new OvershootInterpolator(1.5f);

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

    private void onThemeReady(boolean needTransAnim) {
        themeReady = true;
        dimCover.setVisibility(View.INVISIBLE);
        mProgressViewHolder.hide();

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
            LauncherAnalytics.logEvent("ColorPhone_Theme_Download_Time", "Time",
                    String.valueOf((System.currentTimeMillis() - startDownloadTime + 999) / DateUtils.SECOND_IN_MILLIS));
        }
    }

    private void onRingtoneReady() {
        if (isDimming()) {
            waitingForThemeReady = true;
            mRingtoneViewHolder.disable();
            return;
        }
        boolean isAnimated = RingtoneHelper.isAnimationFinish(mTheme.getId());
        boolean isActive = RingtoneHelper.isActive(mTheme.getId());

        boolean needAutoPlay = Ap.Ringtone.isAutoPlay();
        final boolean isCurrentTheme = isCurrentTheme();

        HSLog.d("Ringtone", "Anim Over: " + isAnimated
         + ", Active: " + isActive + ", autoPlay: " + needAutoPlay);
        if (isAnimated) {
            if (isActive) {
                if (isCurrentTheme && !RingtoneHelper.isDefaultRingtone(mTheme)) {
                    // User change ringtone from outside. We uncheck it
                    RingtoneHelper.ringtoneActive(mTheme.getId(), false);
                    mRingtoneViewHolder.unSelect();
                } else {
                    mRingtoneViewHolder.selectNoAnim();
                }
            } else {
                mRingtoneViewHolder.unSelect();
            }
        } else {
            RingtoneHelper.ringtoneAnim(mTheme.getId());
            if (needAutoPlay) {
                if (isCurrentTheme) {
                    // 设置主题
                    RingtoneHelper.setDefaultRingtoneInBackground(mTheme);
                }
                RingtoneHelper.ringtoneActive(mTheme.getId(), true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRingtoneViewHolder.toastLimit(isCurrentTheme);
                    }
                }, 1000);

                mRingtoneViewHolder.selectNoAnim();

                if (resumed) {
                    startRingtone();
                }
            } else {
                mRingtoneViewHolder.unSelect();
            }

            mRingtoneViewHolder.hello();
        }
    }

    @DebugLog
    private void onThemeApply() {
        ThemeRecommendManager.getInstance().putAppliedThemeForAllUser(mTheme.getIdName());
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

        if (mTheme.getId() == Theme.RANDOM_THEME ||
                !GuideApplyThemeActivity.start(mActivity, true, null)) {
            Utils.showToast(mActivity.getString(R.string.apply_success));
        }

        // Ringtone enabled
        if (mRingtoneViewHolder.isSelect()) {
            RingtoneHelper.setDefaultRingtoneInBackground(mTheme);
        } else {
            RingtoneHelper.resetDefaultRingtone();
        }

        Ap.Ringtone.onApply(mTheme);
        if (!TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Apply", mTheme.getIdName());
            LauncherAnalytics.logEvent(event,
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

    private void stopRingtone() {
        final MediaPlayer mediaPlayer = mActivity.getMediaPlayer();
        mediaPlayer.stop();
    }

    private void startRingtone() {
        final TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(mTheme.getId());
        final MediaPlayer mediaPlayer = mActivity.getMediaPlayer();
        try {

            mediaPlayer.reset();

            HSLog.d("Ringtone", ringtoneModel.getPath());
            mediaPlayer.setDataSource(ringtoneModel.getPath());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
        } else if (ModuleUtils.needShowSetForOneGuide()) {
            ViewStub stub = findViewById(R.id.guide_for_set_one);
            final View guideView = stub.inflate();
            guideView.setAlpha(0);
            guideView.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
            guideView.setOnClickListener(new OnClickListener() {
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
                    LauncherAnalytics.logEvent("Colorphone_Theme_Unlock_Clicked", "from", "detail_page", "themeName", mTheme.getName());
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
        int pHeight = Dimensions.getPhoneHeight(mActivity);



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
        /**
         * Flag for theme loading, ringtone will loading with theme data.
         */
        boolean themeLoading = false;
        if (model != null) {
            // GIf/Mp4

            if (TasksManager.getImpl().isDownloaded(model)) {
                onThemeReady(playTrans);
            } else {
                mDownloadTasks.put(DownloadTask.TYPE_THEME, new DownloadTask(model, DownloadTask.TYPE_THEME));
                themeLoading = true;
                startDownloadTime = System.currentTimeMillis();
                onThemeLoading();
            }
        } else {
            // Directly applicable
            onThemeReady(playTrans);
        }

        if (!mTheme.hasRingtone()) {
            mRingtoneViewHolder.hide();
        } else if (ringtoneModel != null)  {
            if (TasksManager.getImpl().isDownloaded(ringtoneModel)) {
                onRingtoneReady();
            } else {
                // Ringtone data not ready yet. If theme data not loads, we load ringtone separately.
                mDownloadTasks.put(DownloadTask.TYPE_RINGTONE, new DownloadTask(ringtoneModel, DownloadTask.TYPE_RINGTONE));
                if (!themeLoading) {
                    downloadRingtone(ringtoneModel);
                }
            }
        } else {
            // Hide ringtone
            mRingtoneViewHolder.hide();
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
            if (mRingtoneViewHolder.isSelect()) {
                stopRingtone();
            }
            resumed = false;
        }
    }

    private void resumeAnimation() {
        if (themeReady) {
            resumed = true;
            previewWindow.playAnimation(mThemeType);
            callActionView.doAnimation();

            if (mRingtoneViewHolder.isSelect()) {
                startRingtone();
            }
        }

        if (mTheme != null && !TextUtils.isEmpty(mTheme.getRingtoneUrl())) {
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Show", mTheme.getIdName());
            LauncherAnalytics.logEvent(event);
        }
        Ap.Ringtone.onShow(mTheme);
    }

    private boolean isSelectedPos() {
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
                    mProgressViewHolder.updateProgressView((int) (percent * 100));
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD;
                    msg.arg1 = DownloadTask.TYPE_THEME;
                    mHandler.sendMessageDelayed(msg, duration);
                }

                final DownloadTask ringtoneTask = mDownloadTasks.get((DownloadTask.TYPE_RINGTONE));
                if (ringtoneTask != null) {
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD;
                    msg.arg1 = DownloadTask.TYPE_RINGTONE;
                    mHandler.sendMessageDelayed(msg, duration);
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
        mRingtoneViewHolder.disable();
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
            LauncherAnalytics.logEvent("Colorphone_Theme_Button_Unlock_show", "themeName", mTheme.getName());
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
                    LauncherAnalytics.logEvent("Colorphone_Theme_Unlock_Success", "from", "detail_page", "themeName", mTheme.getName());
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
                    LauncherAnalytics.logEvent("Colorphone_Rewardvideo_show", "from", "detail_page", "themeName", mTheme.getName());
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

    private class RingtoneViewHolder {
        private View imageView;
        private LottieAnimationView mLottieAnimationView;
        private TextView toastView;
        private int helloTimes = 0;

        public RingtoneViewHolder() {
            imageView = findViewById(R.id.ringtone_image);
            mLottieAnimationView = findViewById(R.id.ringtone_lottie_open);
            toastView = findViewById(R.id.ringtone_toast);

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }

        private void showToast(boolean afterApply) {
            HSLog.d("Ringtone", "start showToast");
            String txt = getContext().getString(afterApply ? R.string.ringtone_hint_after_apply : R.string.ringtone_hint_before_apply);
            toastView.setVisibility(VISIBLE);
            toastView.setText(txt);
            toastView.setAlpha(0.1f);
            toastView.setScaleX(0.1f);
            toastView.setScaleY(0.1f);
            toastView.post(new Runnable() {
                @Override
                public void run() {
                    toastView.setPivotX(Math.max(toastView.getWidth(), Dimensions.pxFromDp(64)));
                    toastView.setPivotY(toastView.getHeight() * 0.4f);
                }
            });

            toastView.animate().scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toastView.setAlpha(1f);
                            toastView.setScaleX(1f);
                            toastView.setScaleY(1f);
                        }
                    })
                    .start();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HSLog.d("Ringtone", "hide Toast");
                    toastView.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toastView.setVisibility(GONE);
                        }
                    });
                }
            }, 3000);

        }

        private void toastLimit(final boolean currentTheme) {
            Utils.doLimitedTimes(new Runnable() {
                @Override
                public void run() {
                    showToast(currentTheme);
                }
            }, currentTheme ? "ringtone_toast_active" : "ringtone_toast_need_apply", 1);

        }

        private void toggle() {
            final boolean currentTheme = isCurrentTheme();
            final boolean currentSelect = imageView.isActivated();

            HSLog.d("Ringtone", "Switch to " + (currentSelect ? "Close" : "Open"));
            String event = String.format(Locale.ENGLISH, "Colorphone_Theme_%s_Detail_Page_Ringtone_Clicked", mTheme.getIdName());
            LauncherAnalytics.logEvent(event,
                    "Type", currentSelect ? "TurnOff" : "TurnOn");

            if (currentSelect) {
                unSelect();
                stopRingtone();
                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        RingtoneHelper.ringtoneActive(mTheme.getId(), false);
                        if (currentTheme) {
                            RingtoneHelper.resetDefaultRingtone();
                        }
                        ContactManager.getInstance().updateRingtoneOnTheme(mTheme, false);
                    }
                });

            } else {
                selectAnim();
                startRingtone();
                toastLimit(isCurrentTheme());

                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        RingtoneHelper.ringtoneActive(mTheme.getId(), true);
                        if (currentTheme) {
                            RingtoneHelper.setDefaultRingtone(mTheme);
                        }
                        ContactManager.getInstance().updateRingtoneOnTheme(mTheme, true);

                    }
                });
            }
        }

        private void hide() {
            mLottieAnimationView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(INVISIBLE);
            imageView.setEnabled(false);
        }

        private void disable() {
            mLottieAnimationView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(false);
        }

        private void hello() {
            mLottieAnimationView.setVisibility(View.VISIBLE);
            mLottieAnimationView.setAnimation("lottie/ringtone_hello.json");
            mLottieAnimationView.playAnimation();
            imageView.setVisibility(INVISIBLE);
            mLottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    helloTimes ++;
                    if (helloTimes >= 2) {
                        imageView.setVisibility(VISIBLE);
                        mLottieAnimationView.setVisibility(INVISIBLE);
                        mLottieAnimationView.setAnimation("lottie/ringtone_open.json", LottieAnimationView.CacheStrategy.Strong);
                    } else {
                        mHandler.sendEmptyMessage(MSG_RINGTONE_HELLO);
                    }
                }
            });
        }

        private void unSelect() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(false);
            mLottieAnimationView.setVisibility(View.INVISIBLE);
        }

        private boolean isSelect() {
            return imageView.isActivated();
        }

        private void selectNoAnim() {
            select(false);
        }

        private void selectAnim() {
            select(true);
        }

        private void select(boolean anim) {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(true);
            if (anim) {
                mLottieAnimationView.setVisibility(View.VISIBLE);
                mLottieAnimationView.setAnimation("lottie/ringtone_open.json", LottieAnimationView.CacheStrategy.Strong);
                mLottieAnimationView.playAnimation();
                HSLog.d("Ringtone", "Animation [open] call play()");
                imageView.setVisibility(INVISIBLE);

                mLottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setVisibility(VISIBLE);
                        mLottieAnimationView.setVisibility(INVISIBLE);
                    }
                });
            } else {
                mLottieAnimationView.setVisibility(View.INVISIBLE);
            }
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
