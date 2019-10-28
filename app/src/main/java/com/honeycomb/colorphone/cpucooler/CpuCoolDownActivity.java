package com.honeycomb.colorphone.cpucooler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.percent.PercentRelativeLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseCenterActivity;
import com.honeycomb.colorphone.boost.AppInfo;
import com.honeycomb.colorphone.boost.BoostAnimationManager;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerConstant;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerUtils;
import com.honeycomb.colorphone.cpucooler.util.CpuPreferenceHelper;
import com.honeycomb.colorphone.cpucooler.view.CircleView;
import com.honeycomb.colorphone.cpucooler.view.SnowView;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.device.common.HSAppFilter;
import com.superapps.util.Dimensions;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.ArrayList;
import java.util.List;

public class CpuCoolDownActivity extends BaseCenterActivity {

    public static final String TAG = "CpuCoolerLog";

    public static final String EXTRA_KEY_SELECTED_APP_LIST = "EXTRA_KEY_SELECTED_APP_LIST";
    public static final String EXTRA_KEY_NEED_SCAN = "EXTRA_KEY_NEED_SCAN";
    public static final String EXTRA_KEY_RESULT_PAGE_TYPE = "RESULT_PAGE_TYPE";

    private static final long DURATION_FADE_IN = 225;
    private static final long DURATION_SNOW_GROW = 3980;
    private static final long DURATION_CIRCLE_ROTATE = 2900;
    private static final long DURATION_ELEMENTS_FADE_OUT = 400;
    private static final long DELAY_ELEMENTS_FADE_OUT = 480;
    private static final long DURATION_APP_FALL = 1000;
    private static final long DELAY_AMONG_APPS = 250;
    private static final int APP_ICON_FALL_HEIGHT = 400;
    private static final float APP_ICON_ROTATE_ARC = 90f;

    private static final long[] TIME_SNOW_FALL_CONTROL_1 = {280, 320, 360};
    private static final long[] TIME_SNOW_FALL_CONTROL_2 = {280, 320, 480};
    private static final long[] TIME_SNOW_FALL_END = {960, 1120, 1280};
    private static final float[] ALPHA_FALL_CONTROL_1 = {0.6f, 0.3f, 0.3f};
    private static final float[] ALPHA_FALL_CONTROL_2 = {0.6f, 0.3f, 0.3f};
    private static final float[] SNOW_FALL_ROTATE_DEGREE = {100, 90, 138};
    private static final float[] SNOW_FALL_START_ANGLE = {200, 200, 0};

    private final int mAvailableHeight = Dimensions.getPhoneHeight(HSApplication.getContext())
            - ActivityUtils.getStatusBarHeight(HSApplication.getContext());
    private final int mScreenWidth = Dimensions.getPhoneWidth(HSApplication.getContext());

    private PercentRelativeLayout containerView;
    private SnowView mSnowView;
    private CircleView mCircleView;
    private RelativeLayout mGrowingSnowLayout;
    private TextView mCleanHintTextView;
    private ImageView[] mAppIconImgs;

    private boolean mIsVisible = false;
    private Runnable mPendingAnimation;
    private boolean mIsStartToResultPage = false;
    private boolean mIsNeedScan;
    private int resultPageType;

    private int mRandomCoolDownInCelsius;

    private List<String> mPackageNameList;
    private List<String> mAppFallPackageNameList;
    private HSAppMemoryManager.MemoryTaskListener mScanListener;
    private BoostAnimationManager mBoostAnimationManager;
    private boolean showToast;
    private Handler handler = new Handler();

    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isCleanFrozen = CpuCoolerUtils.isCpuCoolerCleanFrozen();
        if(isCleanFrozen) {
            HSLog.d(CpuCoolDownActivity.TAG, "CpuCoolDownActivity isCleanFrozen = true");
            CpuPreferenceHelper.setShouldResultDisplayTemperature(false);
            startDoneActivity();
            return;
        }
        CpuPreferenceHelper.setShouldResultDisplayTemperature(true);
        initData();
        setContentView(R.layout.activity_cpu_cooldown);
        initView();

        AcbAds.getInstance().setActivity(this);
        AcbInterstitialAdManager.getInstance().setForegroundActivity(this);
        ResultPageManager.getInstance().preloadResultPageAds();

        startElementsFadeInAnimation();
        CpuPreferenceHelper.setLastCpuCoolerCleanStartTime();
    }

    private void initData() {
        Intent intent = getIntent();
        if (null != intent) {
            mIsNeedScan = intent.getBooleanExtra(EXTRA_KEY_NEED_SCAN, false);
            mPackageNameList = intent.getStringArrayListExtra(EXTRA_KEY_SELECTED_APP_LIST);
            resultPageType = intent.getIntExtra(EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_CPU_COOLER);
        }

        mBoostAnimationManager = new BoostAnimationManager(0f, 0f);
        mAppFallPackageNameList = mBoostAnimationManager.getBoostDrawablePackageList(CpuCoolDownActivity.this);

        if (mIsNeedScan) {
            startScanApp();
        }
    }

    private void startScanApp() {
        CpuPreferenceHelper.setIsScanCanceled(false);
        HSAppMemoryManager.getInstance().setScanGlobalAppFilter(new HSAppFilter()
                .excludeNonLaunchable()
                .excludeLauncher()
                .excludeInputMethod());
        mScanListener = new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int processCount, int total, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long totalScannedSize) {
                if (null != list) {
                    HSLog.d(CpuCoolDownActivity.TAG, "Cool Down scan list size = " + list.size());
                    if (null == mPackageNameList) {
                        mPackageNameList = new ArrayList<>();
                    } else {
                        mPackageNameList.clear();
                    }
                    for (HSAppMemory app : list) {
                        String packageName = app.getPackageName();
                        mPackageNameList.add(packageName);
                    }
                }
            }

            @Override
            public void onFailed(int i, String s) {
            }
        };
        HSAppMemoryManager.getInstance().startScanWithoutProgress(mScanListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsVisible = true;
        if (mPendingAnimation != null) {
            Runnable pendingAnimation = mPendingAnimation;
            mPendingAnimation = null;
            pendingAnimation.run();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPendingAnimation = null;
        if (null != mScanListener) {
            HSAppMemoryManager.getInstance().stopScan(mScanListener);
            if (mIsNeedScan) {
                CpuPreferenceHelper.setIsScanCanceled(!mIsStartToResultPage);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startClean() {
        if (null == mPackageNameList || mPackageNameList.size() == 0) {
            mPackageNameList = mBoostAnimationManager.getBoostDrawablePackageList(CpuCoolDownActivity.this);
        }

        List<HSAppMemory> apps = new ArrayList<>();
        if (null != mPackageNameList) {
            for (String packageName : mPackageNameList) {
                apps.add(new HSAppMemory(packageName));
            }
        }

        HSAppMemoryManager.getInstance().startClean(apps, new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
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

    private void startElementsFadeInAnimation() {
        mCircleView.postDelayed(new Runnable() {
            @Override public void run() {
                ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0, 1);
                fadeInAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mCircleView.setVisibility(View.VISIBLE);
                        mGrowingSnowLayout.setVisibility(View.VISIBLE);
                        mCleanHintTextView.setVisibility(View.VISIBLE);
                        mRandomCoolDownInCelsius = CpuCoolerManager.getInstance().getRandomCoolDownTemperature();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSnowView.setNeedGrow(true);
                        mSnowView.startRotateAnimation(DURATION_SNOW_GROW, null);

                        startSnowFallAnimation();
                        mCircleView.postDelayed(new Runnable() {
                            @Override public void run() {
                                startAppFallAnimations();
                            }
                        }, 280);

                        mCircleView.startAnimation(DURATION_CIRCLE_ROTATE, new Runnable() {
                            @Override public void run() {
                                handler.postDelayed(new Runnable() {
                                    @Override public void run() {
                                        Runnable next = new Runnable() {
                                            @Override public void run() {
                                                if (mIsNeedScan) {
                                                    startClean();
                                                }
                                                mCircleView.startFadeOutAnimation(DURATION_ELEMENTS_FADE_OUT, null);
                                                startElementsFadeOutAnimation();
                                                handler.postDelayed(new Runnable() {
                                                    @Override public void run() {
                                                        startResultActivity();
                                                    }
                                                }, DURATION_ELEMENTS_FADE_OUT);
                                            }
                                        };

                                        if (mIsVisible) {
                                            next.run();
                                        } else {
                                            mPendingAnimation = next;
                                        }

                                    }
                                }, DELAY_ELEMENTS_FADE_OUT);
                            }
                        });
                    }
                });
                fadeInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();
                        mCircleView.setTranslationY((1 - fraction) * 100);
                        mCircleView.setAlpha(fraction);
                        mCleanHintTextView.setTranslationY((1 - fraction) * 100);
                        mCleanHintTextView.setAlpha(fraction);
                        mGrowingSnowLayout.setTranslationY((1 - fraction) * 100);
                        mGrowingSnowLayout.setAlpha(fraction);
                    }
                });
                fadeInAnimator.setDuration(DURATION_FADE_IN).start();
            }
        }, CpuCoolerConstant.DELAY_ANIMATION_FADE_IN);
    }

    private void startSnowFallAnimation() {
        Analytics.logEvent("CPUCooler_CoolAnimation_Start");
        final View[] fallingSnow = {findViewById(R.id.left_falling_snow_view),
                findViewById(R.id.middle_falling_snow_view),
                findViewById(R.id.right_falling_snow_view)};
        final float[] fallStartY = {getResources().getFraction(R.fraction.cpu_left_falling_snow_start_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_middle_falling_snow_start_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_right_falling_snow_start_y, mAvailableHeight, 1)};
        final float[] fallEndY = {getResources().getFraction(R.fraction.cpu_left_falling_snow_end_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_middle_falling_snow_end_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_right_falling_snow_end_y, mAvailableHeight, 1)};

        final long maxDuration = Math.max(TIME_SNOW_FALL_END[1], TIME_SNOW_FALL_END[2]);
        fallingSnow[0].postDelayed(new Runnable() {
            @Override public void run() {
                CpuCoolDownActivity.this.startSingleSnowFallAnimation(0, fallingSnow[0], fallStartY[0], fallEndY[0]);
            }
        }, (long) (0.9f * maxDuration));
        fallingSnow[1].postDelayed(new Runnable() {
            @Override public void run() {
                CpuCoolDownActivity.this.startSingleSnowFallAnimation(1, fallingSnow[1], fallStartY[1], fallEndY[1]);
            }
        }, (long) (0.4f * maxDuration));
        fallingSnow[2].postDelayed(new Runnable() {
            @Override public void run() {
                CpuCoolDownActivity.this.startSingleSnowFallAnimation(2, fallingSnow[2], fallStartY[2], fallEndY[2]);
            }
        }, 0);
    }

    private void startSingleSnowFallAnimation(final int i, final View snow,final float fallStartY,final float fallEndY) {
        ValueAnimator fallAnimator = ValueAnimator.ofFloat(0, 1);
        final long maxDuration = Math.max(TIME_SNOW_FALL_END[1], TIME_SNOW_FALL_END[2]);
        fallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float relativeFraction = animation.getAnimatedFraction() * maxDuration / TIME_SNOW_FALL_END[i];
                snow.setTranslationY(fallStartY + relativeFraction * (fallEndY - fallStartY));
                snow.setRotation(SNOW_FALL_START_ANGLE[i] + relativeFraction * SNOW_FALL_ROTATE_DEGREE[i]);

                long playTime = animation.getCurrentPlayTime();
                if (playTime < TIME_SNOW_FALL_CONTROL_1[i]) {
                    snow.setAlpha(ALPHA_FALL_CONTROL_1[i] * playTime / TIME_SNOW_FALL_CONTROL_1[i]);
                } else if (playTime < TIME_SNOW_FALL_CONTROL_2[i]) {
                    snow.setAlpha(ALPHA_FALL_CONTROL_1[i] + (ALPHA_FALL_CONTROL_2[i] - ALPHA_FALL_CONTROL_1[i]) * (playTime - TIME_SNOW_FALL_CONTROL_1[i]) / (TIME_SNOW_FALL_CONTROL_2[i] - TIME_SNOW_FALL_CONTROL_1[i]));
                } else if (playTime < TIME_SNOW_FALL_END[i]) {
                    snow.setAlpha(ALPHA_FALL_CONTROL_2[i] * (1 - (float) (playTime - TIME_SNOW_FALL_CONTROL_2[i]) / (TIME_SNOW_FALL_END[i] - TIME_SNOW_FALL_CONTROL_2[i])));
                } else {
                    snow.setAlpha(0);
                }
            }
        });
        fallAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                snow.setVisibility(View.VISIBLE);
                snow.setTranslationY(fallStartY);
                snow.setRotation(SNOW_FALL_START_ANGLE[i]);
            }
        });
        fallAnimator.setDuration(maxDuration).start();
    }

    private void startAppFallAnimations() {
        if (null != mAppFallPackageNameList) {
            for (int i = 0; i < 4 && i < mAppFallPackageNameList.size(); i++) {
//                mAppIconImgs[i].setImageDrawable(LauncherAppState.getInstance().getIconCache().getIconForApp(mAppFallPackageNameList.get(i)));
                // TODO : get AppIcon
                AppInfo info = SystemAppsManager.getInstance().getAppInfoByPkgName(mAppFallPackageNameList.get(i));
                mAppIconImgs[i].setImageDrawable(info != null ? info.getIcon() : null);
                final int finalI = i;
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        CpuCoolDownActivity.this.startSingleIconFallAnimation(finalI);
                    }
                }, i * DELAY_AMONG_APPS);
            }
        }
    }

    private void startSingleIconFallAnimation(int i) {
        final int rotateDirection = Math.random() > 0.5 ? 1 : -1;
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mAppIconImgs[i], "alpha", 0f, 1f, 0f);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mAppIconImgs[i].setTranslationY(fraction * APP_ICON_FALL_HEIGHT);
                mAppIconImgs[i].setRotation(rotateDirection * fraction * APP_ICON_ROTATE_ARC);
            }
        });
        alphaAnimator.setDuration(DURATION_APP_FALL);
        mAppIconImgs[i].setVisibility(View.VISIBLE);
        alphaAnimator.start();
    }

    private void startElementsFadeOutAnimation() {
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0, 1);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();

                float alpha = 1 - fraction;
                alpha = alpha > 0.4f ? 1f : alpha;

                mGrowingSnowLayout.setAlpha(alpha);
                mGrowingSnowLayout.setScaleX(1 + fraction * 0.5f);
                mGrowingSnowLayout.setScaleY(1 + fraction * 0.5f);
                mCleanHintTextView.setAlpha(1 - fraction);
            }
        });
        alphaAnimator.setDuration(DURATION_ELEMENTS_FADE_OUT).start();
    }

    private void startResultActivity() {
        mIsStartToResultPage = true;
        HSLog.d(CpuCoolDownActivity.TAG, "Cpu cool down startResultActivity mRandomCoolDownInCelsius = " + mRandomCoolDownInCelsius);
        NotificationManager.getInstance().updateCpuCoolerCoolDown(mRandomCoolDownInCelsius);
        NotificationManager.getInstance().autoUpdateCpuCoolerTemperature();
        ResultPageActivity.startForCpuCooler(CpuCoolDownActivity.this, resultPageType);
        finish();
    }

    private void initView() {
        containerView = (PercentRelativeLayout) findViewById(R.id.container_view);
        containerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mSnowView = (SnowView) findViewById(R.id.growing_snow_view);
        mCircleView = (CircleView) findViewById(R.id.circle_view);
        mGrowingSnowLayout = (RelativeLayout) findViewById(R.id.growing_snow_layout);
        mCleanHintTextView = (TextView) findViewById(R.id.close_app_hint_tv);

        mAppIconImgs = new ImageView[]{(ImageView) findViewById(R.id.app_icon_img_1), (ImageView) findViewById(R.id.app_icon_img_2),
                (ImageView) findViewById(R.id.app_icon_img_3), (ImageView) findViewById(R.id.app_icon_img_4)};

        int appIconBound = (int) getResources().getFraction(R.fraction.cpu_falling_app_icon_bound, mScreenWidth, 1);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(appIconBound, appIconBound);
        llp.weight = 1;
        for (ImageView appIconImg : mAppIconImgs) {
            appIconImg.setLayoutParams(llp);
            appIconImg.setVisibility(View.INVISIBLE);
        }
        ActivityUtils.setupTransparentSystemBarsForLmpNoNavigation(this);
    }

    private void startDoneActivity() {
        HSLog.d(CpuCoolDownActivity.TAG, "Cpu cool down startDoneActivity");
        ResultPageActivity.startForCpuCooler(CpuCoolDownActivity.this, resultPageType);
        finish();
    }

    @Override public void onBackPressed() {
        if (HSConfig.optBoolean(true, "Application", "CleanGuide", "ForbiddenBackWhenCleaning")) {
            super.onBackPressed();
        } else {
            if (!showToast) {
                showToast = true;
                Toast.makeText(this, R.string.clean_toast_not_back, Toast.LENGTH_SHORT).show();

                handler.postDelayed(() -> showToast = false, 2000);
            }
        }
    }
}
