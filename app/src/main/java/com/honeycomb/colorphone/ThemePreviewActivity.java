package com.honeycomb.colorphone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;


public class ThemePreviewActivity extends HSAppCompatActivity {

    public static final String NOTIFY_THEME_SELECT = "notify_theme_select";
    public static final String NOTIFY_THEME_SELECT_KEY = "notify_theme_select_key";

    private static final int MSG_HIDE = 1;
    private static final int MSG_SHOW = 2;
    private static final long AUTO_HIDE_TIME = 4000;
    private static final long ANIMATION_DURATION = 400;

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;

    private ProgressViewHolder mProgressViewHolder;
    private Button mApplyButton;
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
                    return true;

                case MSG_SHOW:
                    showNavView(true);
                    fadeInActionView();
                    return true;

                default:
                    return false;

            }
        }
    });
    private OvershootInterpolator mInter;

    public static void start(Context context, Theme theme) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("theme", theme);
        context.startActivity(starter);
    }

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            playTransAnimation();
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
            Toast.makeText(ThemePreviewActivity.this, "Paused!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
            final float percent = sofar
                    / (float) total;
            mProgressViewHolder.updateProgressView((int) (percent * 100));
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheme = (Theme) getIntent().getSerializableExtra("theme");
        Type[] types = Type.values();
        for (Type t : types) {
            if (t.getValue() == mTheme.getThemeId()) {
                mThemeType = t;
                break;
            }
        }
        ColorPhoneApplication.getConfigLog().getEvent().onThemePreviewOpen(mThemeType.name().toLowerCase());
        setContentView(R.layout.activity_theme_preview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        previewWindow = (ThemePreviewWindow) findViewById(R.id.card_flash_preview_window);
        previewWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (themeReady && !inTransition) {
                    boolean isInHide = mApplyButton.getTranslationY() == bottomBtnTransY;
                    if (isInHide) {
                        mHandler.sendEmptyMessage(MSG_SHOW);
                    }
                    scheduleNextHide();

                }
                return true;
            }
        });
        callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        callActionView.setTheme(mThemeType);
        callActionView.setAutoRun(false);
        mApplyButton = (Button) findViewById(R.id.theme_apply_btn);
        mProgressViewHolder = new ProgressViewHolder();
        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
        dimCover = findViewById(R.id.dim_cover);

        mNavBack = findViewById(R.id.nav_back);
        mNavBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inTransition) {
                    return;
                }
                CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, mTheme.getThemeId());
                // notify
                HSBundle bundle = new HSBundle();
                bundle.putInt(NOTIFY_THEME_SELECT_KEY, mTheme.getThemeId());
                HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);

                setButtonState(true);
                Toast.makeText(ThemePreviewActivity.this, R.string.apply_success, Toast.LENGTH_SHORT).show();
            }
        });
        bottomBtnTransY = mApplyButton.getTranslationY();

        mInter = new OvershootInterpolator(1.5f);

    }

    private void playTransAnimation() {
        mProgressViewHolder.fadeOut();
        dimCover.animate().alpha(0).setDuration(200);
        mApplyButton.setVisibility(View.VISIBLE);
        animationDelay = 0;
        setButtonState(false);

    }

    @Override
    public void onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped();
    }

    private void onThemeReady() {
        boolean curTheme = CPSettings.getInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, -1) == mTheme.getThemeId();

        setButtonState(curTheme);
    }

    private void setButtonState(final boolean curTheme) {
        themeReady = true;

        dimCover.setVisibility(View.INVISIBLE);
        mProgressViewHolder.hide();
        previewWindow.playAnimation(mThemeType);
        callActionView.doAnimation();

        if (curTheme) {
            mApplyButton.setText(getString(R.string.theme_current));
        } else {
            mApplyButton.setText(getString(R.string.theme_apply));
        }
        mApplyButton.setEnabled(!curTheme);

        fadeInActionView();
    }

    private void showNavView(boolean show) {
        float offsetX = Utils.pxFromDp(60);
        mNavBack.animate().translationX(show ? 0 : -offsetX).setDuration(ANIMATION_DURATION).setInterpolator(mInter).start();
    }

    private void fadeInActionView() {
        inTransition = true;
        mApplyButton.animate().translationY(0).setDuration(ANIMATION_DURATION).setInterpolator(mInter).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mApplyButton.setTranslationY(0);
                inTransition = false;
                animationDelay = 0;
                scheduleNextHide();

            }
        }).setStartDelay(animationDelay).start();
    }

    private void fadeOutActionView() {
        inTransition = true;
        mApplyButton.animate().translationY(bottomBtnTransY).setDuration(ANIMATION_DURATION).setInterpolator(mInter).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mApplyButton.setTranslationY(bottomBtnTransY);
                inTransition = false;

            }
        }).start();
    }

    private void scheduleNextHide() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, AUTO_HIDE_TIME);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(mTheme.getThemeId());
        if (model != null) {
            curTaskId = model.getId();
            // GIf
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (TasksManager.getImpl().isDownloaded(status)) {
                onThemeReady();
            } else {
                onThemeLoading(model);
            }
        } else {
            // Directly applicable
            onThemeReady();
        }

        // Show background if gif drawable not ready.
        if (mTheme != null) {
            if (mTheme.getImageRes() > 0) {
                previewImage.setImageResource(mTheme.getImageRes());
                previewImage.setBackgroundColor(Color.TRANSPARENT);
            } else if (!mThemeType.isGif()){
                previewImage.setImageDrawable(null);
                previewImage.setBackgroundColor(Color.BLACK);
            }

        }
    }

    private void onThemeLoading(TasksManagerModel model) {
        float percent = TasksManager.getImpl().getDownloadProgress(model.getId());
        mProgressViewHolder.updateProgressView((int) (percent * 100));
        FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mDownloadStateListener);

        dimCover.setVisibility(View.VISIBLE);
        previewWindow.updateThemeLayout(mThemeType);
    }


    @Override
    protected void onStop() {
        super.onStop();
        callActionView.stopAnimations();
        previewWindow.stopAnimations();
        FileDownloadMultiListener.getDefault().removeStateListener(curTaskId);

        mNavBack.animate().cancel();
        mApplyButton.animate().cancel();
        mHandler.removeCallbacksAndMessages(null);
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
            mProgressTxt.setText(getString(R.string.loading_progress, percent));
        }

        public void hide() {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressTxtGroup.setVisibility(View.INVISIBLE);
        }

        public void fadeOut() {
            mProgressBar.animate().alpha(0).setDuration(300).start();
            mProgressTxtGroup.animate().alpha(0).setDuration(300).start();
        }
    }
}
