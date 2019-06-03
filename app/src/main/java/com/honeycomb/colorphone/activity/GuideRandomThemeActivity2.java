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
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.theme.RandomTheme;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

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
    private TextSwitcher switcher;
    private final String day4;
    private final String day3;
    private final String day2;
    private final String day1;
    Calendar c = Calendar.getInstance();

    {
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        day1 = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        day2 = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        day3 = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        day4 = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }


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
        View topLayout = findViewById(R.id.guide_random_top_layout);

        LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginLayoutParams.setMargins(left, 0, left, 0);
        mPreview.setLayoutParams(marginLayoutParams);

        ViewGroup.MarginLayoutParams topLayoutLayoutParams = (ViewGroup.MarginLayoutParams) topLayout.getLayoutParams();
        topLayoutLayoutParams.setMargins(0, top, 0, 0);
        topLayout.setLayoutParams(topLayoutLayoutParams);

        TextView firstLineTextView = (TextView) findViewById(com.acb.call.R.id.first_line);
        TextView secondLineTextView = (TextView) findViewById(com.acb.call.R.id.second_line);

        int phoneWidth = Dimensions.getPhoneWidth(this);
        float ratio = (float) width / phoneWidth;
        float shadowOffset = Dimensions.pxFromDp(2) * ratio;
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

        switcher = findViewById(R.id.date_switcher);
        switcher.getLayoutParams().width = width;
        switcher.requestLayout();

        switcher.setInAnimation(this, R.anim.random_theme_text_in);
        switcher.setOutAnimation(this, R.anim.random_theme_text_out);

        switcher.addView(createDayView());
        switcher.addView(createDayView());
        switcher.setCurrentText(day1);

        videoPlayerView.setFileDirectory(file.getAbsolutePath());
        videoPlayerView.playManually();
        videoPlayerView.addProgressListener(progressCallback);

    }

    VideoPlayerView.ProgressCallback progressCallback = new VideoPlayerView.ProgressCallback() {
        int lastPercent = 0;


        @Override
        public void onProgress(int percent) {
            if (lastPercent < 18 && percent >= 18) {
                switcher.setText(day2);
            } else if (lastPercent < 42 && percent >= 42) {
                switcher.setText(day3);
            } else if (lastPercent < 66 && percent >= 66) {
                switcher.setText(day4);
            } else if (lastPercent < 91 && percent >= 91) {
                switcher.setText(day1);
            }
            lastPercent = percent;
        }
    };

    private View createDayView() {
        TextView tv1 = new TextView(this);
        tv1.setAlpha(0.56f);
        tv1.setTextSize(18);
        tv1.setTextColor(Color.WHITE);
        tv1.setGravity(Gravity.CENTER);
        tv1.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD));
        return tv1;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LauncherAnalytics.logEvent("random_theme_guide_back_click_round2","Time", getEventTimeValue());
        releaseMedia();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayerView.removeProgressListener(progressCallback);
    }

    private void releaseMedia() {
        videoPlayerView.release();
    }
}
