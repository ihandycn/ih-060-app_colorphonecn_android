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
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.call.constant.CPConst;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.acb.utils.ConcurrentUtils;
import com.acb.utils.ToastUtils;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorphone.lock.util.CommonUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.IOException;
import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_SELECT;

/**
 * Created by sundxing on 17/8/4.
 */

public class ThemePreviewView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final int MSG_DOWNLOAD = 10;

    private static final boolean PLAY_ANIMITION = true;
    private static final boolean NO_ANIMITION = false;

    private static final long AUTO_HIDE_TIME = 4000;
    private static final long ANIMATION_DURATION = 400;
    private static final long WINDOW_ANIM_DURATION = 400;
    private static final int TRANS_IN_DURATION = 400;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;

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

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE:
                    showNavView(false);
                    fadeOutActionView();
                    for (ThemePreviewView preV : mActivity.getViews()) {
                        int viewPos = (int) preV.getTag();
                        if (viewPos != mPageSelectedPos) {
                            preV.fadeOutActionViewImmediately();
                        }
                    }
                    return true;

                case MSG_SHOW:
                    showNavView(true);
                    fadeInActionView();
                    for (ThemePreviewView preV : mActivity.getViews()) {
                        int viewPos = (int) preV.getTag();
                        if (viewPos != mPageSelectedPos) {
                            preV.fadeInActionViewImmediately();
                        }
                    }
                    return true;

                case MSG_DOWNLOAD: {
                    final int type = msg.arg1;
                    DownloadTask task = mDownloadTasks.get(type);
                    if (task != null) {
                        if (isSelectedPos()) {
                            task.setStatus(DownloadTask.DOWNLOADING);
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
            if (!mBlockAnimationForPageChange) {
                resumeAnimation();
                mBlockAnimationForPageChange = true;
            }

            boolean curTheme = CPSettings.getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
            animationDelay = 0;
            setButtonState(curTheme);
            playButtonAnimation();
        }
    };

    public static void saveThemeApplys(int themeId) {
        if (isThemeAppliedEver(themeId)) {
            return;
        }
        StringBuilder sb = new StringBuilder(4);
        String pre = HSPreferenceHelper.getDefault().getString(ColorPhoneActivity.PREFS_THEME_APPLY, "");
        sb.append(pre).append(themeId).append(",");
        HSPreferenceHelper.getDefault().putString(ColorPhoneActivity.PREFS_THEME_APPLY, sb.toString());
    }

    public static boolean isThemeAppliedEver(int themeId) {
        String[] themes = HSPreferenceHelper.getDefault().getString(ColorPhoneActivity.PREFS_THEME_APPLY, "").split(",");
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
        mApplyButton.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        mActionLayout = findViewById(R.id.theme_apply_layout);
        mApplyForOne = findViewById(R.id.theme_set_for_one);
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
                onThemeApply();
            }
        });

        mApplyForOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsActivity.startSelect(mActivity, mTheme);
                LauncherAnalytics.logEvent("Colorphone_SeletContactForTheme_Started", "ThemeName", mTheme.getIdName());
            }
        });
        bottomBtnTransY = getTransBottomLayout().getTranslationY();

        mInter = new OvershootInterpolator(1.5f);

    }

    private View getTransBottomLayout() {
        return mActionLayout;
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

        setCustomStyle();

        if (needTransAnim) {
            playTransInAnimation(transEndRunnable);
        } else {
            transEndRunnable.run();
        }
    }

    private void onRingtoneReady() {

        boolean isAnimated = RingtoneHelper.isAnimationFinish(mTheme.getId());
        boolean isActive = RingtoneHelper.isActive(mTheme.getId());

        boolean needAutoPlay = Ap.Ringtone.isAutoPlay();
        final boolean isCurrentTheme = isCurrentTheme();

        HSLog.d("Ringtone", "Anim Over: " + isAnimated
         + ", Active: " + isActive + ", autoPlay: " + needAutoPlay);
        if (isAnimated) {
            if (isActive) {
                mRingtoneViewHolder.selectNoAnim();
            } else {
                mRingtoneViewHolder.unSelect();
            }
        } else {
            RingtoneHelper.ringtoneAnim(mTheme.getId());
            if (needAutoPlay) {
                if (isCurrentTheme) {
                    // 设置主题
                    RingtoneHelper.setDefaultRingtone(mTheme);
                }
                RingtoneHelper.ringtoneActive(mTheme.getId(), true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRingtoneViewHolder.toastLimit(isCurrentTheme);
                    }
                }, 1000);

                mRingtoneViewHolder.selectNoAnim();
            } else {
                mRingtoneViewHolder.unSelect();
            }

            mRingtoneViewHolder.hello();
        }
    }

    @DebugLog
    private void onThemeApply() {
        saveThemeApplys(mTheme.getId());
        CPSettings.putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, mTheme.getId());
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

        if (!GuideApplyThemeActivity.start(mActivity, true, null)) {
            Utils.showToast(mActivity.getString(R.string.apply_success));
        }

        // Ringtone enabled
        if (mRingtoneViewHolder.isSelect()) {
            RingtoneHelper.setDefaultRingtone(mTheme);
        }

        Ap.Ringtone.onApply(mTheme);
        NotificationUtils.logThemeAppliedFlurry(mTheme);

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
        if (ModuleUtils.needShowSetForOneGuide()) {
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

    private void playTransInAnimation(final Runnable completeRunnable) {
        View callName = findViewById(R.id.first_line);
        View numberName = findViewById(R.id.second_line);

        View userView = findViewById(R.id.caller_avatar_container);
        if (userView == null) {
            userView = findViewById(R.id.caller_avatar);
        }

        if (mNoTransition) {
            if (completeRunnable != null) {
                completeRunnable.run();
            }
            return;
        }

        int pHeight = Utils.getPhoneHeight(mActivity);
        final View[] animViews = new View[] {userView, callName, numberName, callActionView};
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
        float offsetX = CommonUtils.isRtl() ?  -Utils.pxFromDp(60) : Utils.pxFromDp(60);
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
        }
    }

    private void fadeInActionView() {
        inTransition = true;
        getTransBottomLayout().animate().translationY(0)
                .setDuration(isSelectedPos() ? ANIMATION_DURATION : 0)
                .setInterpolator(mInter)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onActionButtonReady();

                    }
                }).setStartDelay(animationDelay).start();

    }

    private void fadeOutActionView() {
        inTransition = true;
        getTransBottomLayout().animate().translationY(bottomBtnTransY).setDuration(isSelectedPos() ? ANIMATION_DURATION : 0).setInterpolator(mInter).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getTransBottomLayout().setTranslationY(bottomBtnTransY);
                inTransition = false;

            }
        }).setStartDelay(0).start();
    }

    public void fadeOutActionViewImmediately() {
        getTransBottomLayout().setTranslationY(bottomBtnTransY);
    }

    public void fadeInActionViewImmediately() {
        if (!themeReady) {
            return;
        }
        onActionButtonReady();
    }

    public void onActionButtonReady() {
        getTransBottomLayout().setTranslationY(0);
        inTransition = false;
        animationDelay = 0;
        if (isSelectedPos()) {
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
            mDownloadTasks.put(DownloadTask.TYPE_THEME, new DownloadTask(model, DownloadTask.TYPE_THEME));

            if (TasksManager.getImpl().isDownloaded(model)) {
                onThemeReady(playTrans);
            } else {
                onThemeLoading();
                themeLoading = true;
            }
        } else {
            // Directly applicable
            onThemeReady(playTrans);
        }

        if (!Ap.Ringtone.isEnable()) {
            if (BuildConfig.DEBUG) {
                ToastUtils.showToast("Ringtone disable");
            }
        } else if (ringtoneModel != null)  {
            mDownloadTasks.put(DownloadTask.TYPE_RINGTONE, new DownloadTask(ringtoneModel, DownloadTask.TYPE_RINGTONE));
            if (TasksManager.getImpl().isDownloaded(ringtoneModel)) {
                onRingtoneReady();
            } else {
                // Ringtone data not ready yet. If theme data not loads, we load ringtone separately.
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
        }
    }

    public void onStop() {
        hasStopped = true;
        pauseAnimation();
//        if (isSelectedPos()) {
//            mNavBack.animate().cancel();
//        }
        getTransBottomLayout().animate().cancel();
        mHandler.removeCallbacksAndMessages(null);
        if (transAnimator != null && transAnimator.isStarted()) {
            transAnimator.removeAllUpdateListeners();
            transAnimator.removeAllListeners();
            transAnimator.end();
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
        }
    }

    private void resumeAnimation() {
        if (themeReady) {
            previewWindow.playAnimation(mThemeType);
            callActionView.doAnimation();

            if (mRingtoneViewHolder.isSelect()) {
                startRingtone();
            }
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
        HSBundle bundle = new HSBundle();
        bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
        HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_DOWNLOAD, bundle);

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
            DownloadTask themeTask = mDownloadTasks.get(DownloadTask.TYPE_THEME);
            if (themeTask != null && themeTask.getStatus() == DownloadTask.PENDING) {
                downloadTheme(themeTask.getTasksManagerModel());
                mDownloadTasks.remove(DownloadTask.TYPE_THEME);
            }

        }
        triggerPageChangeWhenIdle = true;

    }

    public void updateButtonState() {
        if (themeReady && navIsShow()) {
            setButtonState(isCurrentTheme());
        }
    }

    private boolean isCurrentTheme() {
        return CPSettings.getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
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
        private LottieAnimationView helloAnimation;
        private LottieAnimationView openAnimation;
        private TextView toastView;

        public RingtoneViewHolder() {
            imageView = findViewById(R.id.ringtone_image);
            helloAnimation = findViewById(R.id.ringtone_lottie_hello);
            openAnimation = findViewById(R.id.ringtone_lottie_open);
            toastView = findViewById(R.id.ringtone_toast);

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }

        private void showToast(boolean afterApply) {
            String txt = getContext().getString(afterApply ? R.string.ringtone_hint_after_apply : R.string.ringtone_hint_before_apply);
            toastView.setVisibility(VISIBLE);
            toastView.setText(txt);
            toastView.setAlpha(0.1f);
            toastView.setScaleX(0.1f);
            toastView.setScaleY(0.1f);
            toastView.animate().scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
            toastView.postDelayed(new Runnable() {
                @Override
                public void run() {
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
            if (imageView.isActivated()) {
                unSelect();
                stopRingtone();
                ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
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
                ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
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
            helloAnimation.setVisibility(View.INVISIBLE);
            openAnimation.setVisibility(View.INVISIBLE);
            imageView.setVisibility(INVISIBLE);
        }

        private void disable() {
            helloAnimation.setVisibility(View.INVISIBLE);
            openAnimation.setVisibility(View.INVISIBLE);
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(false);
        }

        private void hello() {
            helloAnimation.setVisibility(View.VISIBLE);
            helloAnimation.setAnimation("lottie/ringtone_hello.json");
            helloAnimation.playAnimation();
            helloAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    imageView.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setVisibility(VISIBLE);
                    helloAnimation.setVisibility(INVISIBLE);
                }
            });
        }

        private void unSelect() {
            imageView.setVisibility(VISIBLE);
            imageView.setEnabled(true);
            imageView.setActivated(false);
            helloAnimation.setVisibility(View.INVISIBLE);
            openAnimation.setVisibility(View.INVISIBLE);
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
                openAnimation.setVisibility(View.VISIBLE);
                openAnimation.setAnimation("lottie/ringtone_open.json");
                openAnimation.playAnimation();
                openAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        imageView.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setVisibility(VISIBLE);
                        openAnimation.setVisibility(INVISIBLE);
                    }
                });
            } else {
                helloAnimation.setVisibility(View.INVISIBLE);
                openAnimation.setVisibility(View.INVISIBLE);
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
