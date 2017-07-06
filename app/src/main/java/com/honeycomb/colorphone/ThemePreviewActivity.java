package com.honeycomb.colorphone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;


public class ThemePreviewActivity extends AppCompatActivity {

    public static final String NOTIFY_THEME_SELECT = "notify_theme_select";
    public static final String NOTIFY_THEME_SELECT_KEY = "notify_theme_select_key";
    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;
    private ProgressBar mProgressBar;
    private TextView mProgressTxt;
    private Button mApplyButton;
    private ImageView previewImage;
    private Theme mTheme;
    private Type mThemeType;
    private View dimCover;
    private int curTaskId;


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
            updateProgressView((int) (percent * 100));
        }
    };

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

        setContentView(R.layout.activity_theme_preview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        previewWindow = (ThemePreviewWindow) findViewById(R.id.card_flash_preview_window);
        callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        mApplyButton = (Button) findViewById(R.id.theme_apply_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.theme_progress_bar);
        mProgressTxt = (TextView) findViewById(R.id.theme_progress_txt);
        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
        dimCover = findViewById(R.id.dim_cover);

        findViewById(R.id.nav_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, mTheme.getThemeId());
                // notify
                HSBundle bundle = new HSBundle();
                bundle.putInt(NOTIFY_THEME_SELECT_KEY, mTheme.getThemeId());
                HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);

                setButtonState(true);
                Toast.makeText(ThemePreviewActivity.this, R.string.apply_success, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void playTransAnimation() {
        mProgressBar.animate().alpha(0).setDuration(300).start();
        mProgressTxt.animate().alpha(0).setDuration(300).start();
        dimCover.animate().alpha(0).setDuration(200);
        mApplyButton.setVisibility(View.VISIBLE);
        mApplyButton.animate().translationY(0).setDuration(400).setInterpolator(new OvershootInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setButtonState(false);
                previewWindow.playAnimation(mThemeType);

            }
        }).start();
    }

    @Override
    public void onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped();
    }

    private void checkButtonState() {
        boolean curTheme = CPSettings.getInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, -1) == mTheme.getThemeId();
        setButtonState(curTheme);
    }

    private void setButtonState(boolean curTheme) {
        dimCover.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressTxt.setVisibility(View.INVISIBLE);
        mApplyButton.setVisibility(View.VISIBLE);
        mApplyButton.setTranslationY(0);
        mApplyButton.setEnabled(true);
        if (curTheme) {
            mApplyButton.setEnabled(false);
            mApplyButton.setText(getString(R.string.theme_current));
        } else {
            mApplyButton.setEnabled(true);
            mApplyButton.setText(getString(R.string.theme_apply));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        previewWindow.startAnimations();
        callActionView.doAnimation();
        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(mTheme.getThemeId());
        if (model != null) {
            curTaskId = model.getId();
            // GIf
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (TasksManager.getImpl().isDownloaded(status)) {
                checkButtonState();
            } else {
                float percent = TasksManager.getImpl().getDownloadProgress(model.getId());
                updateProgressView((int) (percent * 100));

                FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mDownloadStateListener);
            }
        } else {
            // Directly applicable
            checkButtonState();
        }

        // Show background if gif drawable not ready.
        if (mTheme != null) {
            previewWindow.playAnimation(mThemeType);

            if (mTheme.getImageRes() > 0) {
                previewImage.setImageResource(mTheme.getImageRes());
                previewImage.setBackgroundColor(Color.TRANSPARENT);
            } else if (!mThemeType.isGif()){
                previewImage.setImageDrawable(null);
                previewImage.setBackgroundColor(Color.BLACK);
            }

        }
    }

    private void updateProgressView(int percent) {
        mProgressBar.setProgress(percent);
        mProgressTxt.setText(getString(R.string.loading_progress, percent));
    }

    @Override
    protected void onStop() {
        super.onStop();
        callActionView.stopAnimations();
        previewWindow.stopAnimations();
        FileDownloadMultiListener.getDefault().removeStateListener(curTaskId);
    }
}
