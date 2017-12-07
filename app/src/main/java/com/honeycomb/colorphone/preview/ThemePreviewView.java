package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.colorphone.lock.util.CommonUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.DownloadViewHolder;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;

import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_SELECT;

/**
 * Created by sundxing on 17/8/4.
 */

public class ThemePreviewView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final long AUTO_HIDE_TIME = 4000;
    private static final long ANIMATION_DURATION = 400;
    private static final long WINDOW_ANIM_DURATION = 400;
    private static final int TRANS_IN_DURATION = 400;
    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;
    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;

    private ThemePreviewActivity mActivity;
    private View mRootView;

    private ProgressViewHolder mProgressViewHolder;
    private Button mApplyButton;
    private View mApplyForOne;
    private View mActionLayout;

    private View mNavBack;

    private ImageView previewImage;
    private Theme mTheme;
    private Type mThemeType;
    private View dimCover;
    private int curTaskId;
    /**
     * If button is playing animation
     */
    private boolean inTransition;
    private boolean themeReady;

    private long animationDelay = 500;
    private float bottomBtnTransY;


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

                default:
                    return false;

            }
        }
    });
    private OvershootInterpolator mInter;
    private ValueAnimator transAnimator;

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
    private int mPosition = -1;
    private int mPageSelectedPos = -1;
    private TasksManagerModel mPendingDownloadModel;
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
        TextView name = (TextView) findViewById(R.id.caller_name);
        TextView number = (TextView) findViewById(R.id.caller_number);
        name.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR));
        number.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));

        name.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(1), Color.BLACK);
        number.setShadowLayer(Utils.pxFromDp(2), 0, Utils.pxFromDp(2), Color.BLACK);

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
        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
        dimCover = findViewById(R.id.dim_cover);

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inTransition) {
                    return;
                }
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

                if (!GuideApplyThemeActivity.start(mActivity, true)) {
                    showToast(mActivity.getString(R.string.apply_success));
                }
                NotificationUtils.logThemeAppliedFlurry(mTheme);

            }
        });

        mApplyForOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsActivity.startSelect(mActivity, mTheme);
            }
        });
        bottomBtnTransY = getTransBottomLayout().getTranslationY();

        mInter = new OvershootInterpolator(1.5f);

    }

    private void showToast(String hint) {
       Toast toast = new Toast(mActivity.getApplicationContext());
        final View contentView = mActivity.getLayoutInflater().inflate(R.layout.toast_theme_apply, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.setElevation(CommonUtils.pxFromDp(8));
        }
        TextView textView = contentView.findViewById(R.id.text_toast);
        textView.setText(hint);
        int yOffset = (int) (0.6f * CommonUtils.getPhoneHeight(mActivity));
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffset);
        toast.setView(contentView);
        toast.show();
    }

    private View getTransBottomLayout() {
        return mActionLayout;
    }


    private void playDownloadOkTransAnimation() {
        mProgressViewHolder.fadeOut();
        dimCover.animate().alpha(0).setDuration(200);
        getTransBottomLayout().setVisibility(View.VISIBLE);
        animationDelay = 0;
        onThemeReady(false);
    }


    Runnable transEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mBlockAnimationForPageChange) {
                previewWindow.playAnimation(mThemeType);
                callActionView.doAnimation();
                mBlockAnimationForPageChange = true;
            }

            boolean curTheme = CPSettings.getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
            animationDelay = 0;
            setButtonState(curTheme);
            playButtonAnimation();
        }
    };

    private void onThemeReady(boolean needTransAnim) {
        themeReady = true;
        dimCover.setVisibility(View.INVISIBLE);
        mProgressViewHolder.hide();
//        if (isSelectedPos()) {
//            mNavBack.setTranslationX(0);
//        }

        previewWindow.updateThemeLayout(mThemeType);
        checkNewFeatureGuideView();
        setCustomStyle();
        if (needTransAnim) {
            playTransInAnimation(transEndRunnable);
        } else {
            transEndRunnable.run();
        }

    }

    private void checkNewFeatureGuideView() {
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
                        }
                    }).start();
                }
            });
        }
    }

    private void playTransInAnimation(final Runnable completeRunnable) {
        View callName = findViewById(R.id.caller_name);
        View numberName = findViewById(R.id.caller_number);

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
                        getTransBottomLayout().setTranslationY(0);
                        inTransition = false;
                        animationDelay = 0;
                        scheduleNextHide();

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
        getTransBottomLayout().setTranslationY(0);
        animationDelay = 0;
        scheduleNextHide();
    }

    private void scheduleNextHide() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, AUTO_HIDE_TIME);
    }

    public void onStart() {
        // We do not play animation if activity restart.
        boolean playTrans = !hasStopped;
        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(mTheme.getId());
        if (model != null) {
            curTaskId = model.getId();
            // GIf
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (TasksManager.getImpl().isDownloaded(status)) {
                onThemeReady(playTrans);
            } else {
                onThemeLoading(model);
            }
        } else {
            // Directly applicable
            onThemeReady(playTrans);
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
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .transition(BitmapTransitionOptions.withCrossFade(200));

                if (ThemePreviewActivity.cacheBitmap != null) {
                    request.placeholder(new BitmapDrawable(getResources(), ThemePreviewActivity.cacheBitmap));
                    ThemePreviewActivity.cacheBitmap = null;
                }

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
        callActionView.stopAnimations();
        previewWindow.stopAnimations();
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
        FileDownloadMultiListener.getDefault().removeStateListener(curTaskId);

    }

    private void pauseAnimation() {
        if (themeReady) {
            previewWindow.stopAnimations();
            callActionView.stopAnimations();
        }
    }

    private void resumeAnimation() {
        if (themeReady) {
            previewWindow.playAnimation(mThemeType);
            callActionView.doAnimation();
        }
    }

    private boolean isSelectedPos() {
        return mPosition == mPageSelectedPos;
    }

    public void setPageSelectedPos(int pos) {
        mPageSelectedPos = pos;
    }

    private void onThemeLoading(final TasksManagerModel model) {
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
                float percent = TasksManager.getImpl().getDownloadProgress(model.getId());
                mProgressViewHolder.updateProgressView((int) (percent * 100));
                if (percent == 0f || Float.isNaN(percent)) {
                    ColorPhoneApplication.getConfigLog().getEvent().onThemeDownloadStart(model.getName().toLowerCase(), ConfigLog.FROM_LIST);
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isSelectedPos()) {
                            downloadTheme(model);
                        } else {
                            mPendingDownloadModel = model;
                        }

                    }
                }, duration);
            }
        });
    }

    private void downloadTheme(TasksManagerModel model) {
        setBlockAnimationForPageChange(false);

        DownloadViewHolder.doDownload(model, null);

        // Notify download status.
        HSBundle bundle = new HSBundle();
        bundle.putInt(NOTIFY_THEME_KEY, mTheme.getId());
        HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_DOWNLOAD, bundle);

        FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mDownloadStateListener);
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
        if ((isSelectedPos() && mPendingDownloadModel != null)) {
            downloadTheme(mPendingDownloadModel);
            mPendingDownloadModel = null;
        }
        triggerPageChangeWhenIdle = true;

    }

    public void updateButtonState() {
        if (themeReady && navIsShow()) {
            boolean curTheme = CPSettings.getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == mTheme.getId();
            setButtonState(curTheme);
        }
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

}
