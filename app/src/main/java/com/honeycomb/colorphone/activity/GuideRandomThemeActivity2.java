package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.utils.FileUtils;
import com.acb.call.views.CircleImageView;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.VideoPlayerView;
import com.acb.utils.Utils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.theme.RandomTheme;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.io.File;

/**
 *
 * @author sundxing
 * @date 17/9/13
 */

public class GuideRandomThemeActivity2 extends HSAppCompatActivity {

    private View mPreview;
    private long mFocusStartTime;
    private View rootLayout;
    private VideoPlayerView videoPlayerView;

    public static void start(Activity context, View contentView, boolean fullScreen) {
        int[] locations = new int[2];
        contentView.getLocationInWindow(locations);
        Intent starter = new Intent(context, GuideRandomThemeActivity2.class);
        starter.putExtra("fullscreen", false);
        starter.putExtra("left", locations[0]);
        starter.putExtra("top", locations[1] - Dimensions.pxFromDp(24));
        starter.putExtra("width", contentView.getWidth());
        starter.putExtra("height", contentView.getHeight());
        context.startActivity(starter);
        context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mFocusStartTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);

        setContentView(R.layout.guide_random_feature2);
        rootLayout = findViewById(R.id.guide_random_root_layout);

        initThemeCard();
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }
        Ap.RandomTheme.logEvent("random_theme_guide_show");
        LauncherAnalytics.logEvent("random_theme_guide_show_round2");

        TextView enableBtn = (TextView) findViewById(R.id.guide_random_ok);
        enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(24), true));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RandomTheme.getInstance().setUserSettingsEnable(true);
                Ap.RandomTheme.logEvent("random_theme_guide_ok_click");
                LauncherAnalytics.logEvent("random_theme_guide_ok_click_round2", "Time", getEventTimeValue());
                releaseMedia();
                finish();
            }
        });

    }

    private String getEventTimeValue() {
        long interval = System.currentTimeMillis() - mFocusStartTime;
        long  second = interval / 1000L;
        if (second <= 2) {
            return "0-2s";
        } else if (second <= 5) {
            return "2-5s";
        } else if (second <= 10) {
            return "5-10s";
        } else {
            return "10+";
        }
    }

    private void initThemeCard() {

        int left = getIntent().getIntExtra("left", 0);
        int top = getIntent().getIntExtra("top", 0);
        int width = getIntent().getIntExtra("width", 0);

        mPreview = findViewById(R.id.card_view);
        LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginLayoutParams.setMargins(left, top, left, 0);
        mPreview.setLayoutParams(marginLayoutParams);


        TextView firstLineTextView = (TextView) findViewById(com.acb.call.R.id.first_line);
        TextView secondLineTextView = (TextView) findViewById(com.acb.call.R.id.second_line);

        int phoneWidth = Utils.getPhoneWidth(this);
        float ratio = (float) width / phoneWidth;
        float shadowOffset = Utils.pxFromDp(2) * ratio;
        firstLineTextView.setShadowLayer(shadowOffset, 0, shadowOffset, Color.BLACK);
        secondLineTextView.setShadowLayer(shadowOffset * 0.5f, 0, shadowOffset * 0.7f, Color.BLACK);

        firstLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32 * ratio);
        secondLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * ratio);

        firstLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getBondFont());
        secondLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getNormalFont());

        View mUserPhotoView = (CircleImageView) mPreview.findViewById(com.acb.call.R.id.caller_avatar);
        mUserPhotoView.setVisibility(View.GONE);

        InCallActionView callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
        callActionView.setAutoRun(true);

        videoPlayerView = findViewById(R.id.animation_view);
        final File file = new File(FileUtils.getMediaDirectory(), Constants.RANDOM_GUIDE_FILE_NAME);

        TextSwitcher switcher = findViewById(R.id.date_switcher);
        switcher.getLayoutParams().width = width;
        switcher.requestLayout();

        switcher.setInAnimation(this, R.anim.random_theme_text_in);
        switcher.setOutAnimation(this, R.anim.random_theme_text_out);

        switcher.addView(createDayView());
        switcher.addView(createDayView());
        switcher.setCurrentText("Monday");

        videoPlayerView.setFileDirectory(file.getAbsolutePath());
        videoPlayerView.playManually();
        videoPlayerView.addProgressListener(new VideoPlayerView.ProgressCallback() {
            int lastPercent = 0;
            @Override
            public void onProgress(int percent) {
                if (lastPercent < 20 && percent >= 20) {
                    switcher.setText("Tuesday");
                } else if (lastPercent < 47 && percent >= 47) {
                    switcher.setText("Wednesday");
                } else if (lastPercent < 75 && percent >= 75) {
                    switcher.setText("Thursday");
                } else if (lastPercent < 99 && percent >= 99) {
                    switcher.setText("Monday");
                }
                lastPercent = percent;
            }
        });

    }

    private View createDayView() {
        TextView tv1 = new TextView(this);
        tv1.setAlpha(0.56f);
        tv1.setTextSize(16);
        tv1.setTextColor(Color.WHITE);
        tv1.setGravity(Gravity.CENTER);
        tv1.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        return tv1;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LauncherAnalytics.logEvent("random_theme_guide_back_click_round2","Time", getEventTimeValue());
        releaseMedia();
    }

    private void releaseMedia() {
        videoPlayerView.release();
    }
}