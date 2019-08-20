package com.honeycomb.colorphone.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.Placements;
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

import net.appcloudbox.ads.base.ContainerView.AcbContentLayout;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.expressad.AcbExpressAdManager;
import net.appcloudbox.ads.expressad.AcbExpressAdView;

public class CleanGuideActivity extends HSAppCompatActivity {
    private static final String TAG = CleanGuideActivity.class.getSimpleName();
    public static final String EXTRA_KEY_CLEAN_TYPE = "extra_key_clean_type";

    private ImageView imageView;
    private TextView title;
    private TextView description;
    private RevealFlashButton action;

    private String exitReason = "Other";
    private boolean isShowNativeAD;

    private boolean mAdShown;

    private FrameLayout mAdContainer;
    private AcbExpressAdView adView;

    public static void start(@CleanGuideCondition.CLEAN_GUIDE_TYPES int type) {
        HSLog.i(CleanGuideCondition.TAG, "CleanGuideActivity.start");
        Intent intent = new Intent(HSApplication.getContext(), CleanGuideActivity.class);
        intent.putExtra(EXTRA_KEY_CLEAN_TYPE, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(HSApplication.getContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            HSLog.i(CleanGuideCondition.TAG, "CleanGuideActivity.start failed");
            e.printStackTrace();
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSLog.i(CleanGuideCondition.TAG, "CleanGuideActivity onCreate");

        isShowNativeAD = HSConfig.optBoolean(true, "Application", "CleanGuide", "PopUpAdEnable");
        if (isShowNativeAD) {
            setContentView(R.layout.clean_guide_activity_with_ad);
            AcbExpressAdManager.getInstance().activePlacementInProcess(Placements.AD_CLEAN_GUIDE);
            AcbExpressAdManager.getInstance().preload(1, Placements.AD_CLEAN_GUIDE);
        } else {
            setContentView(R.layout.clean_guide_activity);

            View view = findViewById(R.id.content_view);
            view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        }


        View close = findViewById(R.id.close_btn);
        close.setOnClickListener(v -> {
            finish();
            exitReason = "Close";
        });

        imageView = findViewById(R.id.clean_image);
        title = findViewById(R.id.clean_title);
        description = findViewById(R.id.clean_description);
        action = findViewById(R.id.clean_action_btn);

        configUI();

        showAdIfProper();
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

                actionRunnable = () -> Navigations.startActivitySafely(this, CpuCoolDownActivity.class);
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
        String type = HSConfig.optString("DismissPopUp", "Application", "CleanGuide", "ResponseToBackWhenPopUp");
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
        if (adView != null) {
            adView.destroy();
        }
    }

    private void showAdIfProper() {
        if (mAdShown) {
            return;
        }

        if (!isShowNativeAD) {
            return;
        }

        showAd();
    }

    public void showAd() {
        if (mAdContainer == null) {
            mAdContainer = findViewById(R.id.ad_fragment);
        }
        addAdView();
    }

    private void addAdView() {
        if (adView == null) {
            adView = new AcbExpressAdView(this, Placements.AD_CLEAN_GUIDE, "");
            AcbContentLayout layout = new AcbContentLayout(com.messagecenter.R.layout.acb_phone_alert_ad_card_big);
            layout.setActionId(com.messagecenter.R.id.ad_call_to_action);
            layout.setChoiceId(com.messagecenter.R.id.ad_conner);
            layout.setTitleId(com.messagecenter.R.id.ad_title);
            layout.setDescriptionId(com.messagecenter.R.id.ad_subtitle);
            layout.setIconId(com.messagecenter.R.id.ad_icon);
            layout.setPrimaryId(com.messagecenter.R.id.ad_cover_img);

            adView.setCustomLayout(layout);
            adView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_All);
            adView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                @Override
                public void onAdClicked(AcbExpressAdView acbExpressAdView) {

                }

                @Override
                public void onAdShown(AcbExpressAdView acbExpressAdView) {
                    mAdShown = true;

                }
            });

            adView.prepareAd(new AcbExpressAdView.PrepareAdListener() {
                @Override
                public void onAdReady(AcbExpressAdView acbExpressAdView, float v) {
                    adView.setGravity(Gravity.CENTER);
                    mAdContainer.setVisibility(View.VISIBLE);
                    mAdContainer.addView(acbExpressAdView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }

                @Override
                public void onPrepareAdFailed(AcbExpressAdView acbExpressAdView, AcbError acbError) {

                }

            });
        }
    }
}
