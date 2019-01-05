package com.honeycomb.colorphone.battery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.util.ActivityUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.device.common.HSAppUsageInfo;
import com.ihs.device.monitor.usage.HSAppUsageInfoManager;
import com.superapps.util.HomeKeyWatcher;

import net.appcloudbox.autopilot.AutopilotConfig;

import java.util.ArrayList;
import java.util.List;


public class BatteryCleanActivity extends BaseAppCompatActivity {

    public static final String EXTRA_KEY_SCANNED_LIST = "scanned_list";
    public static final String EXTRA_KEY_SAVE_TIME = "save_time";
    public static final String EXTRA_KEY_COME_FROM_MAIN_PAGE = "come_from_main_page";

    private static final int DEFAULT_APP_COUNT_ANIM_INTERVAL = 200;

    private boolean mFromMainPage;

    private Handler handler = new Handler();

    private PercentRelativeLayout containerView;
    private View mTitleBattery;
    private View mTitleClean;
    private View mBackArrow;

    private ViewGroup cleanLayout;

    private ImageView inDotCircleImageView;
    private ImageView outDotCircleImageView;

    private ImageView cleanScaleCircle;

    private ViewGroup cleanCountLayout;
    private TextView descriptionTextView;

    private TextView sumTextView;
    private TextView cleanCountTextView;
    private TextView mScanResult;

    private RelativeLayout iconAppNameLayout;
    private ImageView iconImageView;
    private TextView appNameTextView;

    private ObjectAnimator inDotCircleRotationAnimator;
    private ObjectAnimator outDotCircleRotationAnimator;

    private List<String> cleanAppList;
    private int saveTime;

    private boolean stopped;

    private int cleanedAppCount = 0;
    private long startTimeMills;
    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean isFromBatteryImprover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.configStatusBarColor(this);

        mFromMainPage = getIntent().getBooleanExtra(EXTRA_KEY_COME_FROM_MAIN_PAGE, false);
        cleanAppList = getIntent().getStringArrayListExtra(EXTRA_KEY_SCANNED_LIST);
        saveTime = getIntent().getIntExtra(EXTRA_KEY_SAVE_TIME, 0);
        isFromBatteryImprover = ResultPageManager.getInstance().isFromBatteryImprover();

        ResultPageManager.preloadResultPageAds();

        setContentView(R.layout.activity_battery_clean);
        initView();
        startAnimation();
        mHomeKeyWatcher = new HomeKeyWatcher(this);

        if (isFromBatteryImprover) {
            LauncherAnalytics.logEvent("ColorPhone_CableImprover_CleanPage_Show");
            Ap.Improver.logEvent("cleanpage_show");
            mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    long runningTime = System.currentTimeMillis() - startTimeMills;
                    Ap.Improver.logEvent("cleanpage_home_click");
                    LauncherAnalytics.logEvent("ColorPhone_CableImprover_CleanPage_Home_Click", "Time", formatTime(runningTime));
                }

                @Override
                public void onRecentsPressed() {

                }
            });
        }
    }

    private String formatTime(long runningTime) {
        long timeSeconds = runningTime / 1000;
        if (timeSeconds < 1) {
            return "0-1s";
        } else if (timeSeconds < 3) {
            return "1-3s";
        } else if (timeSeconds < 5) {
            return "3-5s";
        }
        return "above5s";
    }

    private void startAnimation() {
        inDotCircleImageView.post(new Runnable() {
            @Override
            public void run() {
                if (mFromMainPage) {
                    mBackArrow.setVisibility(View.VISIBLE);
                    mTitleClean.setVisibility(View.VISIBLE);
                    mTitleBattery.animate().alpha(0).setDuration(240).
                            setListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mBackArrow.animate().alpha(1).setDuration(240).start();
                                    mTitleClean.animate().alpha(1).setDuration(240).start();
                                }
                            }).start();
                }

                ValueAnimator valueAnimatorText = ValueAnimator.ofFloat(0.3f, 1);
                valueAnimatorText.setDuration(360);
                valueAnimatorText.setInterpolator(new FastOutSlowInInterpolator());
                valueAnimatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();

                        if (mFromMainPage) {
                            cleanCountLayout.setAlpha((value - 0.3f) / 0.7f);
                        }
                        descriptionTextView.setAlpha((value - 0.3f) / 0.7f);

                        cleanCountLayout.setScaleX(value);
                        cleanCountLayout.setScaleY(value);
                        descriptionTextView.setScaleX(value);
                        descriptionTextView.setScaleY(value);
                    }
                });
                valueAnimatorText.start();

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.6f, 1f);
                valueAnimator.setDuration(320);
                valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();

                        inDotCircleImageView.setAlpha((value - 0.6f) / 0.4f);
                        outDotCircleImageView.setAlpha((value - 0.6f) / 0.4f);

                        inDotCircleImageView.setScaleX(value);
                        inDotCircleImageView.setScaleY(value);

                        outDotCircleImageView.setScaleX(value);
                        outDotCircleImageView.setScaleY(value);
                    }
                });

                valueAnimator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        inDotCircleRotationAnimator = ObjectAnimator.ofFloat(inDotCircleImageView, "Rotation", 0, -360);
                        inDotCircleRotationAnimator.setRepeatMode(ValueAnimator.RESTART);
                        inDotCircleRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        inDotCircleRotationAnimator.setDuration(10000).setInterpolator(new LinearInterpolator());
                        inDotCircleRotationAnimator.start();

                        outDotCircleRotationAnimator = ObjectAnimator.ofFloat(outDotCircleImageView, "Rotation", 360, 0);
                        outDotCircleRotationAnimator.setRepeatMode(ValueAnimator.RESTART);
                        outDotCircleRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        outDotCircleRotationAnimator.setDuration(1000).setInterpolator(new LinearInterpolator());
                        outDotCircleRotationAnimator.start();

                        if (mFromMainPage) {
                            startClean();
                        } else {
                            startScan();
                        }
                    }
                });

                valueAnimator.start();
            }
        });
    }

    private void initView() {
        containerView = (PercentRelativeLayout) findViewById(R.id.container_view);
        containerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mTitleBattery = findViewById(R.id.title_battery);
        mTitleClean = findViewById(R.id.title_clean);
        mBackArrow = findViewById(R.id.back_arrow);

        cleanLayout = (ViewGroup) findViewById(R.id.clean_layout);

        inDotCircleImageView = (ImageView) findViewById(R.id.in_dot_circle);
        outDotCircleImageView = (ImageView) findViewById(R.id.out_dot_circle);

        cleanScaleCircle = (ImageView) findViewById(R.id.clean_scale_circle);

        cleanCountLayout = (ViewGroup) findViewById(R.id.count_layout);
        descriptionTextView = (TextView) findViewById(R.id.description);

        sumTextView = (TextView) findViewById(R.id.sum);
        cleanCountTextView = (TextView) findViewById(R.id.clean_count);
        mScanResult = (TextView) findViewById(R.id.scan_result);

        iconAppNameLayout = (RelativeLayout) findViewById(R.id.icon_name);
        iconImageView = (ImageView) findViewById(R.id.icon);
        appNameTextView = (TextView) findViewById(R.id.app_name);
        View skipBtn = findViewById(R.id.skip_button);
        if (showShowSkipButton()) {
            skipBtn.setVisibility(View.VISIBLE);
        } else {
            skipBtn.setVisibility(View.GONE);
        }
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSkipPressed();
            }
        });

        if (mFromMainPage) {
            mTitleBattery.setVisibility(View.VISIBLE);
            mBackArrow.setVisibility(View.INVISIBLE);
            mTitleClean.setVisibility(View.INVISIBLE);
            descriptionTextView.setText(R.string.battery_clean_description);
            sumTextView.setText(String.valueOf(cleanAppList.size()));
            cleanCountTextView.setText(String.valueOf(0));
        } else {
            mTitleBattery.setVisibility(View.INVISIBLE);
            mBackArrow.setVisibility(View.VISIBLE);
            mTitleClean.setVisibility(View.VISIBLE);
            descriptionTextView.setText(R.string.battery_scaning);
            sumTextView.setText(String.valueOf(String.valueOf(0)));
            cleanCountTextView.setText(String.valueOf(0));
            mScanResult.setText(String.valueOf(0));
        }

        mBackArrow.setOnClickListener((view) -> {
            if (view.getVisibility() == View.VISIBLE) {
                BatteryUtils.setShouldReturnToLauncherFromResultPage(true);
                finish();
            }
        });
    }

    private boolean showShowSkipButton() {
        if (!isFromBatteryImprover) {
            return false;
        }
        boolean config = HSConfig.optBoolean(false, "Application", "ChargingImprover", "CleanPageShowSkipBtn");
        boolean autopilot = AutopilotConfig.getBooleanToTestNow(Ap.Improver.TOPIC_ID, "clean_page_show_skip_btn", false);
        HSLog.d("SkipButton", "config : " + config + "; autopilot : " + autopilot);
        return false;
    }

    private void startScan() {
        cleanAppList = new ArrayList<>();
        HSAppUsageInfoManager.getInstance().startAppUsageScan(new HSAppUsageInfoManager.AppUsageTaskListener() {

            @Override
            public void onSucceeded(List<HSAppUsageInfo> list) {
                HSLog.d(BatteryUtils.TAG, "Scan Succeeded: " + list.size());
                if (stopped) {
                    HSLog.d(BatteryUtils.TAG, "Skip after-scan work since user has left");
                    return;
                }
                if (list.size() == 0) {
                    ResultPageActivity.startForBattery(BatteryCleanActivity.this, true, 0, 0);
                    finish();
                    return;
                }

                cleanAppList.addAll(BatteryUtils.getSizeLimitedScanResultPackageNames(list));
                int appCount = cleanAppList.size();
                for (HSAppUsageInfo hsAppUsageInfo : list) {
                    saveTime += hsAppUsageInfo.getEstimateSaveMinutes();
                }
                sumTextView.setText(String.valueOf(appCount));
                ValueAnimator countIncreaseanimator = ValueAnimator.ofFloat(0f, 1f);
                countIncreaseanimator.setDuration(appCount * DEFAULT_APP_COUNT_ANIM_INTERVAL);
                countIncreaseanimator.setInterpolator(new LinearInterpolator());
                countIncreaseanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int currentCount = (int) (appCount * animation.getAnimatedFraction());
                        mScanResult.setText(String.valueOf(currentCount));
                    }
                });
                countIncreaseanimator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mScanResult.post(() -> {
                            mScanResult.setText(String.valueOf(appCount));
                            int[] sumLocation = new int[2];
                            sumTextView.getLocationInWindow(sumLocation);
                            int[] resultLocation = new int[2];
                            mScanResult.getLocationInWindow(resultLocation);
                            mScanResult.animate().translationX(sumLocation[0] - resultLocation[0]).setDuration(600)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            cleanCountLayout.animate().alpha(1).setDuration(500)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            super.onAnimationEnd(animation);
                                                            mScanResult.setVisibility(View.INVISIBLE);
                                                            descriptionTextView.setText(R.string.battery_clean_description);
                                                            BatteryUtils.setBatteryLastCleanSecondTime(System.currentTimeMillis() / 1000);
                                                            startClean();
                                                        }
                                                    }).start();
                                        }
                                    }).start();
                        });

                    }
                });
                countIncreaseanimator.start();
            }

            @Override
            public void onFailed(int i, String s) {
                HSLog.d(BatteryUtils.TAG, "Scan failed: " + s);
            }
        });
    }

    private void startClean() {
        List<HSAppMemory> hsAppMemoryList = new ArrayList<>();
        for (String item : cleanAppList) {
            hsAppMemoryList.add(new HSAppMemory(item));
        }

        HSAppMemoryManager.getInstance().startClean(hsAppMemoryList, new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
                LauncherAnalytics.logEvent("Battery_CleanAnimation_Show");
                startCleanAnimator();
            }

            @Override
            public void onProgressUpdated(int i, int i1, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long l) {
            }

            @Override
            public void onFailed(int i, String s) {
            }
        });

    }

    private void startCleanAnimator() {

        if (cleanAppList.isEmpty()) {

            if (inDotCircleRotationAnimator != null && inDotCircleRotationAnimator.isRunning()) {
                inDotCircleRotationAnimator.cancel();
            }

            if (outDotCircleRotationAnimator != null && outDotCircleRotationAnimator.isRunning()) {
                outDotCircleRotationAnimator.cancel();
            }

            cleanLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(cleanLayout, "alpha", 1f, 0f);
            objectAnimator.setDuration(600);
            objectAnimator.start();
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cleanLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    if (mFromMainPage) {
                        BatteryUtils.setBatteryLastCleanSecondTime(System.currentTimeMillis() / 1000);
                    }

                    if (stopped) {
                        HSLog.d(BatteryUtils.TAG, "Skip result page since user has left");
                    } else {
                        boolean isBatteryOptimal = saveTime == 0;
                        int hour = saveTime / 60;
                        int minute = saveTime % 60;
                        ResultPageActivity.startForBattery(BatteryCleanActivity.this, isBatteryOptimal, hour, minute);
                    }
                    finish();
                }
            });

            return;
        }

//        IconCache iconCache = LauncherAppState.getInstance().getIconCache();
//        appNameTextView.setText(iconCache.getTitleForApp(cleanAppList.get(0)));
//        iconImageView.setBackgroundDrawable(iconCache.getIconForApp(cleanAppList.get(0)));
        // TODO: get App name and icon
        appNameTextView.setText(Utils.getAppTitle(cleanAppList.get(0)));
        iconImageView.setBackgroundDrawable(Utils.getAppIcon(cleanAppList.get(0)));

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iconAppNameLayout, "alpha", 0, 1);
        objectAnimator.setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.4f);
                        valueAnimator.setDuration(360);
                        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());

                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float value = (float) animation.getAnimatedValue();

                                cleanScaleCircle.setScaleX(value);
                                cleanScaleCircle.setScaleY(value);
                                cleanScaleCircle.setAlpha(value);

                                cleanScaleCircle.setAlpha(1 - (1 - value) / 0.6f);
                            }
                        });
                        valueAnimator.start();

                    }
                }, 200);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final float translationY = iconAppNameLayout.getY() - (cleanLayout.getY() + cleanLayout.getHeight() / 2);

                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, translationY);
                        valueAnimator.setDuration(360);
                        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float value = (float) animation.getAnimatedValue();

                                iconAppNameLayout.setTranslationY(-value);
                                iconAppNameLayout.setAlpha(1 - value / translationY);
                                iconAppNameLayout.setScaleX(1 - value / translationY * 0.3f);
                                iconAppNameLayout.setScaleY(1 - value / translationY * 0.3f);

                            }
                        });

                        valueAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                iconAppNameLayout.setTranslationY(0);
                                iconAppNameLayout.setScaleX(1);
                                iconAppNameLayout.setScaleY(1);

                                if (cleanAppList.size() > 0) {
                                    cleanAppList.remove(0);
                                }

                                cleanedAppCount++;
                                cleanCountTextView.setText(String.valueOf(cleanedAppCount));

                                startCleanAnimator();
                            }
                        });
                        valueAnimator.start();
                    }
                }, 280);
            }
        });

        objectAnimator.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        stopped = false;
        startTimeMills = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopped = true;
    }

    @Override
    public void onBackPressed() {
        boolean canBack = true;
        if (ResultPageManager.getInstance().isFromBatteryImprover()) {
            canBack = HSConfig.optBoolean(true, "Application", "ChargingImprover", "CleanAllowBack")
            && AutopilotConfig.getBooleanToTestNow(Ap.Improver.TOPIC_ID, "clean_allow_back", false);
            boolean backToResult = HSConfig.optBoolean(false, "Application", "ChargingImprover", "CleanClickBackToResultPage")
                    && AutopilotConfig.getBooleanToTestNow(Ap.Improver.TOPIC_ID, "clean_click_back_to_result_page", false);
            if (canBack && backToResult) {
                ResultPageActivity.startForBattery(this, true, 0, 0);
            }
            logTimeConsumes(System.currentTimeMillis() - startTimeMills);

        }
        if (canBack) {
            super.onBackPressed();
        }
    }

    /**
     * Skip clean animation
     */
    public void onSkipPressed() {
        Ap.Improver.logEvent("cleanpage_skip_click");
        ResultPageActivity.startForBattery(this, true, 0, 0);
        finish();
    }

    private void logTimeConsumes(long showTime) {
        Ap.Improver.logEvent("cleanpage_back_click");
        LauncherAnalytics.logEvent("ColorPhone_CableImprover_CleanPage_Back_Click", "Time", formatTime(showTime));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inDotCircleRotationAnimator != null && inDotCircleRotationAnimator.isRunning()) {
            inDotCircleRotationAnimator.cancel();
        }

        if (outDotCircleRotationAnimator != null && outDotCircleRotationAnimator.isRunning()) {
            outDotCircleRotationAnimator.cancel();
        }

        handler.removeCallbacksAndMessages(null);

        if (mHomeKeyWatcher != null) {
            mHomeKeyWatcher.stopWatch();
        }
    }

    public static String getAppTitle(String packageName) {
        String title = "";
        try {
            PackageManager pm = HSApplication.getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            if (info != null) {
                title = info.loadLabel(pm).toString();
                if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(info.name)) {
                    title = info.name;
                }

                if (TextUtils.isEmpty(title)) {
                    title = packageName;
                }
            }
        } catch (Exception ignored) {
        }
        return title;
    }
}
