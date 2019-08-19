package com.honeycomb.colorphone.notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.battery.BatteryCleanActivity;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.cpucooler.CpuCoolDownActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.RevealFlashButton;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

public class CleanGuideActivity extends HSAppCompatActivity {
    private static final String TAG = CleanGuideActivity.class.getSimpleName();
    public static final String EXTRA_KEY_CLEAN_TYPE = "extra_key_clean_type";

    private ImageView imageView;
    private TextView title;
    private TextView description;
    private RevealFlashButton action;

    private String exitReason = "Other";

    public static void start(@CleanGuideCondition.CLEAN_GUIDE_TYPES int type) {
        Intent intent = new Intent(HSApplication.getContext(), CleanGuideActivity.class);
        intent.putExtra(EXTRA_KEY_CLEAN_TYPE, type);
        Navigations.startActivitySafely(HSApplication.getContext(), intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.clean_guide_activity);

        View view = findViewById(R.id.content_view);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));

        view = findViewById(R.id.close_btn);
        view.setOnClickListener(v -> {
            finish();
            exitReason = "Close";
        });

        imageView = findViewById(R.id.clean_image);
        title = findViewById(R.id.clean_title);
        description = findViewById(R.id.clean_description);
        action = findViewById(R.id.clean_action_btn);

        configUI();
    }

    private void configUI() {
        if (getIntent() == null) {
            HSLog.i(TAG, "configUI NO intent, finish");
            finish();
            return;
        }

        @CleanGuideCondition.CLEAN_GUIDE_TYPES
        int type = getIntent().getIntExtra(EXTRA_KEY_CLEAN_TYPE, CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_LOW);

        int descriptionRes;
        int imageRes;
        int actionColor;
        final Runnable actionRunnable;
        SpannableString titleText;

        String highlight;
        String titleStr;
        int index;

        switch (type) {
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_APPS:
                imageRes = R.drawable.clean_guide_battery_apps;
                descriptionRes = R.string.clean_guide_description_battery_apps;
                actionColor = 0xff5abc6e;

                highlight = getString(R.string.clean_guide_title_battery_apps_highlight);
                titleStr = getString(R.string.clean_guide_title_battery_apps);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Intent intent = new Intent(this, BatteryCleanActivity.class);
                    intent.putExtra("sss", "s");
                    Navigations.startActivitySafely(this, BatteryCleanActivity.class);
                };
                break;
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_LOW:
                imageRes = R.drawable.clean_guide_battery_low;
                descriptionRes = R.string.clean_guide_description_battery_low;
                actionColor = 0xff5abc6e;

                highlight = DeviceManager.getInstance().getBatteryLevel() + "%";
                titleStr = String.format(getString(R.string.clean_guide_title_battery_low), highlight);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Intent intent = new Intent(this, BatteryCleanActivity.class);
                    intent.putExtra("sss", "s");
                    Navigations.startActivitySafely(this, intent);
                };
                break;
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_APPS:
                imageRes = R.drawable.clean_guide_boost_apps;
                descriptionRes = R.string.clean_guide_description_boost_apps;
                actionColor = 0xff007ef5;

                highlight = String.valueOf(DeviceManager.getInstance().getRunningApps());
                titleStr = String.format(getString(R.string.clean_guide_title_boost_apps), highlight);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                highlight = getString(R.string.clean_guide_title_boost_apps_highlight);
                index = titleStr.indexOf(highlight);
                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Intent intent = new Intent(this, BoostActivity.class);
                    intent.putExtra("sss", "s");
                    Navigations.startActivitySafely(this, intent);
                };
                break;
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_JUNK:
                imageRes = R.drawable.clean_guide_boost_junk;
                descriptionRes = R.string.clean_guide_description_boost_junk;
                actionColor = 0xff007ef5;

                highlight = DeviceManager.getInstance().getJunkSize();
                titleStr = String.format(getString(R.string.clean_guide_title_boost_junk), highlight);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Intent intent = new Intent(this, BoostActivity.class);
                    intent.putExtra("sss", "s");
                    Navigations.startActivitySafely(this, intent);
                };
                break;
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_MEMORY:
                imageRes = R.drawable.clean_guide_boost_memory;
                descriptionRes = R.string.clean_guide_description_boost_memory;
                actionColor = 0xff007ef5;

                highlight = DeviceManager.getInstance().getRamUsage() + "%";
                titleStr = String.format(getString(R.string.clean_guide_title_boost_memory), highlight);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Intent intent = new Intent(this, BoostActivity.class);
                    intent.putExtra("sss", "s");
                    Navigations.startActivitySafely(this, intent);
                };
                break;
            default:
            case CleanGuideCondition.CLEAN_GUIDE_TYPE_CPU_HOT:
                imageRes = R.drawable.clean_guide_cpu_hot;
                descriptionRes = R.string.clean_guide_description_cpu_hot;
                actionColor = 0xff58b8ff;

                highlight = getString(R.string.clean_guide_title_cpu_hot_highlight);
                titleStr = getString(R.string.clean_guide_title_cpu_hot);
                index = titleStr.indexOf(highlight);
                titleText = new SpannableString(titleStr);

                titleText.setSpan(
                        new ForegroundColorSpan(0xffd43d3d),
                        index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                actionRunnable = () -> {
                    Navigations.startActivitySafely(this, CpuCoolDownActivity.class);
                };
                break;
        }

        imageView.setImageResource(imageRes);
        description.setText(descriptionRes);
        title.setText(titleText);
        action.setBackground(BackgroundDrawables.createBackgroundDrawable(actionColor, Dimensions.pxFromDp(6), true));
        action.setOnClickListener(v -> {
            finish();
            actionRunnable.run();
            Analytics.logEvent("Clean_Guide_Click", "Type", "Guide" + type);
            exitReason = "OKBtn";
        });

        startButtonAppearAnimation();

        Analytics.logEvent("Clean_Guide_Show", "Type", "Guide" + type);
    }

    private void startButtonAppearAnimation() {
        action.setVisibility(View.VISIBLE);
        action.setFlashDuration(560);
        action.postDelayed(() -> action.flash(), 260);
    }

    @Override public void onBackPressed() {
        String type = HSConfig.optString("：DismissPopUp", "Application", "CleanGuide", "ResponseToBackWhenPopUp");
        if (TextUtils.equals(type, "DismissPopUp")) {
            exitReason = "Back";
            super.onBackPressed();
        } else if (TextUtils.equals(type, "ContunieCleaning")) {
            action.performClick();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Analytics.logEvent("Clean_Guide_Close", "Type", exitReason);
    }
}
