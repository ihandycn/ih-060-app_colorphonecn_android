package com.honeycomb.colorphone.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.RevealFlashButton;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;

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

    private HomeKeyWatcher homeKeyWatcher;

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

        isShowNativeAD = false;
        if (isShowNativeAD) {
            setContentView(R.layout.clean_guide_activity_with_ad);
            AcbExpressAdManager.getInstance().activePlacementInProcess(Placements.getAdPlacement(Placements.AD_CLEAN_GUIDE));
            AcbExpressAdManager.getInstance().preload(1, Placements.getAdPlacement(Placements.AD_CLEAN_GUIDE));

            View view = findViewById(R.id.content_view);
            view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(6), false));

            view = findViewById(R.id.ad_fragment);
            view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(6), false));

        } else {
            setContentView(R.layout.clean_guide_activity);

            View view = findViewById(R.id.content_view);
            view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        View close = findViewById(R.id.close_btn);
        close.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(20), true));
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

        homeKeyWatcher = new HomeKeyWatcher(this);
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override public void onHomePressed() {
                finish();
            }

            @Override public void onRecentsPressed() {

            }
        });
    }

    private void configUI() {
        if (getIntent() == null) {
            HSLog.i(TAG, "configUI NO intent, finish");
            finish();
            return;
        }

        @CleanGuideCondition.CLEAN_GUIDE_TYPES
        int type = getIntent().getIntExtra(EXTRA_KEY_CLEAN_TYPE, CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_LOW);

        CleanGuideCondition.CleanGuideInfo info = new CleanGuideCondition.CleanGuideInfo(this, type);

        imageView.setImageResource(info.imageRes);
        description.setText(info.descriptionStr);
        title.setText(info.titleText);

        action.setText(info.actionStr);
        action.setBackground(BackgroundDrawables.createBackgroundDrawable(info.actionColor, Dimensions.pxFromDp(6), true));
        action.setOnClickListener(v -> {
            finish();
            info.actionRunnable.run();
            exitReason = "OKBtn";
        });

        startButtonAppearAnimation();

    }

    private void startButtonAppearAnimation() {
        action.setVisibility(View.VISIBLE);
        action.setFlashDuration(770);
        action.setFlashInterpolator(PathInterpolatorCompat.create(.69f, 0f, .56f, .76f));
        action.postDelayed(() -> action.flash(), 200);
        action.postDelayed(() -> action.flash(), 1170);
        action.postDelayed(() -> action.flash(), 2940);
        action.postDelayed(() -> action.flash(), 3910);
    }

    @Override public void onBackPressed() {
        exitReason = "Back";
        super.onBackPressed();
    }

    @Override protected void onStart() {
        super.onStart();
        homeKeyWatcher.startWatch();
    }

    @Override protected void onStop() {
        super.onStop();
        homeKeyWatcher.stopWatch();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
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
        Analytics.logEvent("Clean_Guide_AD_Should_Show");

        if (adView == null) {
            adView = new AcbExpressAdView(this, Placements.getAdPlacement(Placements.AD_CLEAN_GUIDE), "");
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
                    Analytics.logEvent("Clean_Guide_AD_Show");
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
