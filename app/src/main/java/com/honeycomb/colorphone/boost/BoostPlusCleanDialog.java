package com.honeycomb.colorphone.boost;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationCondition;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.util.ViewStyleUtils;
import com.honeycomb.colorphone.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BoostPlusCleanDialog extends FullScreenDialog {

    public static final String TAG = BoostPlusCleanDialog.class.getSimpleName();

    public static final int CLEAN_TYPE_NON_ROOT = 0;
    public static final int CLEAN_TYPE_ROOT = 1;
    public static final int CLEAN_TYPE_NON_ROOT_DIRECTLY = 2;
    public static final int CLEAN_TYPE_NON_ROOT_ACCESSIBILITY_OPEN = 3;
    public static final int CLEAN_TYPE_NORMAL = 4;
    public static final int CLEAN_TYPE_TOOLBAR = 5;
    public static final int CLEAN_TYPE_CLEAN_CENTER = 6;

    private static final int DEFAULT_ACC_CLEAN_TIMEOUT_SECONDS = 5;

    public static final long FRAME = 35; // 30 fps
    private static final long FRAME_DOTS = 40;

    // Circle
    private static final long DURATION_CIRCLE_IN_ALPHA_ADD = 28 * FRAME;
    private static final long DURATION_CIRCLE_IN_ALPHA_REDUCE = 18 * FRAME;

    public static final long START_OFF_CIRCLE_ROTATE_MAIN = 0;
    private static final long DURATION_ROTATE_MAIN = 100 * FRAME;

    // Clean Size
    private static final long DURATION_MEMORY_USED_ALPHA_REDUCE = 5 * FRAME;
    private static final long DURATION_CLEAN_ITEM_APP = 10 * FRAME;
    private static final long DURATION_CLEAN_ITEM_APP_END_LAST = 5 * FRAME;

    public static final int DEVICE_SCREEN_HEIGHT_TAG = 1920;

    // Cleaning running apps
    private static final long DURATION_CLEANING_TEXT_START_OFF = START_OFF_CIRCLE_ROTATE_MAIN + 10 * FRAME;

    private static final long TIMEOUT_CLEAN = 2 * 60 * 1000;
    private static final long TIMEOUT_GET_PERMISSION = 60 * 1000;

    // Dots
    private static final int DOTS_COUNT = 80; // one dots 200ms

    // Boosted Circle
    private static final float CIRCLE_ROTATE_FACTOR = 1.5f;

    // Background
    private static final long DURATION_BACKGROUND_SINGLE_CHANGED = 50 * FRAME;
    private static final long DURATION_BACKGROUND_END_CHANGED = 25 * FRAME;

    private static final int END_SIZE_LAST_PERCENT = 3;
    private static final long DURATION_END_SIZE_DECELERATE_FACTOR = 3;
    private static final long START_OFF_EXIT_CLEAN = 1000;
    private static final long TIMEOUT_EXITING_DIALOG = 12000;
    private static final long DELAY_IMG_CURVE_ANIMATION = 500;

    RelativeLayout mCleanMainRl;
    RelativeLayout mBoostIconContainer;
    private RelativeLayout mExitingRl;
    LinearLayout mBoostingTextLl;
    View mContainerV;
//    private View mStopDialogV;

    ImageView mCircleInIV;
    ImageView mCircleMiddleIv;
    ImageView mCircleOutIv;
    ImageView mDotPositionTagIv;
    private ImageView mBoostCenterIv;
    private ImageView mIconOneV;
    private ImageView mIconTwoV;
    private ImageView mIconThreeV;
    private ImageView mIconFourV;
    private ImageView mIconFiveV;
    private ImageView mIconSixV;
    private ImageView mIconSevenV;
    AppCompatImageView mDotTagIv;

    private BoostTextView mMemoryUsedNumberTv;

    private ProgressWheel mExitingProgressWheel;

    Handler mHandler = new Handler();
    Handler mDotsHandler = new Handler();

    private Runnable mCleanTimeOutRunnable;
    private Runnable mGetPermissionTimeOutRunnable;

    boolean mIsResultViewShow;
    private boolean mIsStartGetPermission;
    boolean mIsPermissionGetting;
    private boolean mIsStartForceStopCancel;
    private boolean mIsStartRootCancel;
    boolean mIsBackDisabled;
    boolean mIsRootCleaning;
    private boolean mIsFlurryLogResultShow;

    private int mScreenHeight;
    int mDotsAnimationCount = 0;
    private long mCurrentLastCleanSize;
    long mStartCircleAnimationTime;

    DynamicRotateAnimation mCircleInDynamicRotateAnimation;
    DynamicRotateAnimation mCircleMiddleDynamicRotateAnimation;
    DynamicRotateAnimation mCircleOutDynamicRotateAnimation;

    private ValueAnimator mBgColorAnimator;

    ArrayList<HSAppMemory> mSelectedAppList;
    List<String> mCleanedAppPackageNameList = new ArrayList<>();
    CleanResult mCleanResult = CleanResult.CLEAN_CANCEL;

    private int mType;
    private DeviceManager.RAMStatus ramStatus;

    boolean mAnimationEndEventLogged;

    private enum CleanResult {
        CLEAN_SUCCESS,
        CLEAN_FAILED,
        CLEAN_CANCEL,
        PERMISSION_SUCCESS,
        PERMISSION_FAILED,
        PERMISSION_CANCEL
    }

    private enum CleanAnimation {
        FIRST,
        SECOND,
        THIRD,
    }

    public static void showBoostPlusCleanDialog(Context context, int type) {
        FloatWindowDialog dialog = new BoostPlusCleanDialog(context, type);
        FloatWindowManager.getInstance().showDialog(dialog);

    }

    public static void hideBoostPlusCleanDialog(Context context, int type) {
        FloatWindowDialog dialog = FloatWindowManager.getInstance().getDialog(BoostPlusCleanDialog.class);
        if (dialog != null) {
            FloatWindowManager.getInstance().removeDialog(dialog);
        }
    }

    public BoostPlusCleanDialog(Context context, int type) {
        this(context);
        ramStatus = DeviceManager.getInstance().getRamStatus();
        mType = initCleanType(type);

//        mSelectedAppList = new ArrayList<>();

        mScreenHeight = Dimensions.getPhoneHeight(context);

        mContainerV = ViewUtils.findViewById(mContentView, R.id.view_container);
        mContainerV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mCleanMainRl = ViewUtils.findViewById(mContentView, R.id.clean_main_rl);
        // init dots animation view tag
        mCleanMainRl.setTag(false);
        // init material content view top margin
        ViewUtils.setMargins(mCleanMainRl, 0, Dimensions.getStatusBarHeight(getContext()), 0, 0);

        mBoostIconContainer = (RelativeLayout) findViewById(R.id.boost_icon);
        mCircleInIV = ViewUtils.findViewById(mContentView, R.id.circle_in_iv);
        mCircleMiddleIv = ViewUtils.findViewById(mContentView, R.id.circle_middle_iv);
        mCircleOutIv = ViewUtils.findViewById(mContentView, R.id.circle_out_iv);
        mMemoryUsedNumberTv = ViewUtils.findViewById(mContentView, R.id.memory_used_number_tv);
        mBoostingTextLl = ViewUtils.findViewById(mContentView, R.id.boosting_text_ll);

        mBoostCenterIv = ViewUtils.findViewById(mContentView, R.id.boost_center_iv);

        mDotTagIv = ViewUtils.findViewById(mContentView, R.id.dot_anchor_tag_iv);
        mDotPositionTagIv = ViewUtils.findViewById(mContentView, R.id.dot_normal_anchor_iv);

        mExitingRl = ViewUtils.findViewById(mContentView, R.id.exiting_rl);
        mExitingProgressWheel = ViewUtils.findViewById(mContentView, R.id.exiting_progress_wheel);

        mIconOneV = ViewUtils.findViewById(mContentView, R.id.boost_icon_1_iv);
        mIconTwoV = ViewUtils.findViewById(mContentView, R.id.boost_icon_2_iv);
        mIconThreeV = ViewUtils.findViewById(mContentView, R.id.boost_icon_3_iv);
        mIconFourV = ViewUtils.findViewById(mContentView, R.id.boost_icon_4_iv);
        mIconFiveV = ViewUtils.findViewById(mContentView, R.id.boost_icon_5_iv);
        mIconSixV = ViewUtils.findViewById(mContentView, R.id.boost_icon_6_iv);
        mIconSevenV = ViewUtils.findViewById(mContentView, R.id.boost_icon_7_iv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View navigationBar = ViewUtils.findViewById(mContentView, R.id.navigation_bar_view);
            FrameLayout.LayoutParams layoutParams = (LayoutParams) navigationBar.getLayoutParams();
            layoutParams.height = Dimensions.getNavigationBarHeight(context);
            navigationBar.setLayoutParams(layoutParams);
        }

        switch (ramStatus) {
            case EMERGENCY:
                mContainerV.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.boost_plus_red));
                break;
            case NORMAL:
                mContainerV.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.boost_plus_yellow));
                break;
            case GOOD:
                mContainerV.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green));
                break;
        }

        startBoostAnimation();
        startClean(mType);
    }

    public BoostPlusCleanDialog(Context context) {
        super(context);
    }

    public BoostPlusCleanDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoostPlusCleanDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int initCleanType(int type) {
        if (type == CLEAN_TYPE_NON_ROOT_DIRECTLY) {
            type = CLEAN_TYPE_NON_ROOT;
        } else if (type == CLEAN_TYPE_NON_ROOT_ACCESSIBILITY_OPEN) {
            type = CLEAN_TYPE_NON_ROOT;
        } else if (type == CLEAN_TYPE_TOOLBAR || type == CLEAN_TYPE_CLEAN_CENTER) {
            int beforePercentage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
            int afterPercentage = RamUsageDisplayUpdater.getInstance().startBoost();
            long cachedAppSize = DeviceManager.getInstance().getTotalRam() * (beforePercentage - afterPercentage) / 100;
            AdUtils.preloadResultPageAds();
            List<String> runningApps = NotificationCondition.getsInstance().getRunningAppsPackageNames();
            if (runningApps != null && runningApps.size() > 0) {
                int cachedAppCount = runningApps.size();
                mSelectedAppList = new ArrayList<>();
                for (int i = 0; i < cachedAppCount; i++) {
                    mSelectedAppList.add(new HSAppMemory(runningApps.get(i), cachedAppSize / cachedAppCount));
                }
            } else {
                int cachedAppCount = (new Random()).nextInt(4) + 1;
                mSelectedAppList = new ArrayList<>();
                for (int i = 0; i < cachedAppCount; i++) {
                    mSelectedAppList.add(new HSAppMemory("memory_app_" + i, cachedAppSize / cachedAppCount));
                }
            }
        }
        return type;
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        windowParams.format = PixelFormat.RGBA_8888;

        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.height = Dimensions.getPhoneHeight(HSApplication.getContext());

            windowParams.gravity = Gravity.TOP;

        } else {
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;

            windowParams.gravity = Gravity.TOP;
        }
        this.setLayoutParams(windowParams);
        return windowParams;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.boost_plus_clean_fullscreen_dialog;
    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    @Override
    public boolean isModalTip() {
        return false;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        initAnimationLocation();

//        Toolbar toolbar = (Toolbar) mContentView.findViewById(R.id.action_bar);
//        if (mType == CLEAN_TYPE_TOOLBAR || mType == CLEAN_TYPE_CLEAN_CENTER) {
//            toolbar.setVisibility(View.GONE);
            mContentView.findViewById(R.id.action_bar_alias).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.iv_back_alias).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mIsBackDisabled) {
                        onBackClicked();
                    }
                }
            });
            TextView titleView = mContentView.findViewById(R.id.iv_title_alias);
            ViewStyleUtils.setToolbarTitleWithoutLayoutParams(titleView);
            titleView.setTypeface(Fonts.getTypeface(Fonts.Font.ROBOTO_MEDIUM));
//        } else {
//            toolbar.setTitle(getContext().getString(R.string.boost_title));
//            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!mIsBackDisabled) {
//                        onBackClicked();
//                    }
//                }
//            });
//        }
    }


    @TargetApi(16)
    private void initAnimationLocation() {
        mDotTagIv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Utils.ATLEAST_JELLY_BEAN) {
                    mDotTagIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mDotTagIv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                Rect location = ViewUtils.getLocationRect(mDotTagIv);
                int top = location.top - Dimensions.getStatusBarHeight(getContext());
                int left = location.left;
                ViewUtils.setMargins(mDotPositionTagIv, left, top, 0, 0);
            }
        });
    }

//    private void initStopDialog() {
//        mStopDialogV = ViewUtils.findViewById(mContentView, R.id.stop_dialog_view);
//        // Stop Dialog title content
//        TextView stopDialogTitleTv = (TextView) findViewById(R.id.custom_alert_title);
//        TextView stopDialogBodyTv = (TextView) findViewById(R.id.custom_alert_body);
//        stopDialogTitleTv.setText(getContext().getString(R.string.boost_plus_stop_clean_title));
//        stopDialogBodyTv.setText(getContext().getString(R.string.boost_plus_stop_clean_content));
//        // Stop Dialog button
//        Button stopDialogCancelBtn = (Button) findViewById(R.id.custom_alert_cancel_btn);
//        stopDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dismissStopDialog();
//            }
//        });
//        Button stopDialogOkBtn = (Button) findViewById(R.id.custom_alert_ok_btn);
//        stopDialogOkBtn.setText(getContext().getString(R.string.boost_plus_stop_sure));
//        stopDialogOkBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (cancelClean()) {
//                    dismissStopDialog();
//                    showExitingDialog();
//                } else {
//                    dismissStopDialog();
//                    dismissDialog();
//                }
//            }
//        });
//    }

    private void showExitingDialog() {
        mIsBackDisabled = true;
        if (null != mExitingRl) {
            mExitingRl.setVisibility(View.VISIBLE);
        }
        spinProgressWheel();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getContext(), "Exiting Time Out...", Toast.LENGTH_SHORT).show();
                }
                dismissExitingDialog();
                onCancelExitClean(true, getCleanRemainingAppList());
            }
        }, TIMEOUT_EXITING_DIALOG);
    }

    private void dismissExitingDialog() {
        mIsBackDisabled = false;
        if (null != mExitingRl) {
            mExitingRl.setVisibility(View.GONE);
        }
        stopProgressWheel();
    }

    private void onCancelExitClean(final boolean isCanceled, final List<HSAppMemory> cleanRemainingApps) {
        mIsStartForceStopCancel = false;
        mIsStartRootCancel = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissExitingDialog();
                if (isCanceled) {
                    onCancelDialogDismiss(cleanRemainingApps);
                } else {
                    dismissDialog();
                }
            }
        }, START_OFF_EXIT_CLEAN);
    }

    public void spinProgressWheel() {
        if (null == mExitingProgressWheel) {
            return;
        }
        mExitingProgressWheel.setFinishSpeed(500f / 360f);
        mExitingProgressWheel.setSpinSpeed(125f / 360f);
        mExitingProgressWheel.setBarSpinCycleTime(START_OFF_EXIT_CLEAN + 150);
        mExitingProgressWheel.setVisibility(VISIBLE);
        mExitingProgressWheel.spin();
    }

    private void stopProgressWheel() {
        if (null == mExitingProgressWheel) {
            return;
        }
        mExitingProgressWheel.stopSpinning();
    }

//    public void showStopDialog() {
//        if (null != mStopDialogV) {
//            mStopDialogV.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private boolean isStopDialogShowing() {
//        boolean isShowing = false;
//        if (null != mStopDialogV) {
//            isShowing = (mStopDialogV.getVisibility() == View.VISIBLE);
//        }
//        return isShowing;
//    }
//
//    private void dismissStopDialog() {
//        if (null != mStopDialogV) {
//            mStopDialogV.setVisibility(View.GONE);
//        }
//    }

    private int getSelectedSize() {
        return null == mSelectedAppList ? 0 : mSelectedAppList.size();
    }

    /*  @Override
      protected FloatWindowManager.Type getType() {
          return FloatWindowManager.Type.BOOST_PLUS_CLEAN;
      }
  */
    @Override
    public boolean onBackPressed() {
        onBackClicked();
        return true;
    }

    public void dismissDialog() {
        dismissDialog(0L);
    }

    public void dismissDialog(long actualDismissDelay) {
        HSLog.d(TAG, "dismissDialog  mType == " + mType);
        if (mType == CLEAN_TYPE_TOOLBAR || mType == CLEAN_TYPE_CLEAN_CENTER) {
            FloatWindowManager.getInstance().removeDialog(this);
            onDialogDismiss();
            return;
        }

        onDialogDismiss();
        Runnable dismissRunnable = new Runnable() {
            @Override
            public void run() {
                FloatWindowManager.getInstance().removeDialog(BoostPlusCleanDialog.this);
            }
        };
        if (actualDismissDelay > 0) {
            mHandler.postDelayed(dismissRunnable, actualDismissDelay);
        } else {
            dismissRunnable.run();
        }
    }

    public void onCancelDialogDismiss(List<HSAppMemory> cleanRemainingApps) {
        onDialogDismiss();
        FloatWindowManager.getInstance().removeDialog(this);
    }

    private boolean cancelClean() {
        boolean cancelStart = false;
        if (mIsRootCleaning) {
            HSLog.d(TAG, "root cancelClean");
            cancelStart = true;
            mIsStartRootCancel = true;
            HSAppMemoryManager.getInstance().stopClean();
        } else {
            if (mIsStartGetPermission && mCleanResult != CleanResult.PERMISSION_SUCCESS) {
                HSLog.d(TAG, "cancelPermission");
                cancelStart = true;
            }
        }
        return cancelStart;
    }

    private void onDialogDismiss() {
        HSLog.d(TAG, "onDialogDismiss mCleanResult = " + mCleanResult + " mIsStartGetPermission = " + mIsStartGetPermission);

        // Notify underlying result page to start its animations
        LauncherAnalytics.logEvent("Colorphone_Boost_Finished");

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mDotsHandler) {
            mDotsHandler.removeCallbacksAndMessages(null);
        }
        if (null != mBgColorAnimator) {
            mBgColorAnimator.cancel();
        }
        if (null != mCircleInIV) {
            mCircleInIV.clearAnimation();
        }
        if (null != mCircleMiddleIv) {
            mCircleMiddleIv.clearAnimation();
        }
        if (null != mCircleOutIv) {
            mCircleOutIv.clearAnimation();
        }
    }

    private void startBoostAnimation() {
        HSLog.d(TAG, "startBoostAnimation ***");
        mIsFlurryLogResultShow = false;
        startCircleRotateAnimation();
        startDotsAnimation();
        startBackgroundAnimation(CleanAnimation.FIRST);

        mCleanTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getContext(), "Clean Time Out...", Toast.LENGTH_SHORT).show();
                }
                startDecelerateResultAnimation();
                mIsRootCleaning = false;
            }
        };
        mHandler.postDelayed(mCleanTimeOutRunnable, TIMEOUT_CLEAN);
    }

    private void cancelCleanTimeOut() {
        HSLog.d(TAG, "cancelCleanTimeOut ***");
        if (null != mCleanTimeOutRunnable) {
            mHandler.removeCallbacks(mCleanTimeOutRunnable);
        }
    }

    private void cancelGetPermissionTimeOut() {
        HSLog.d(TAG, "cancelGetPermissionTimeOut ***");
        if (null != mGetPermissionTimeOutRunnable) {
            mHandler.removeCallbacks(mGetPermissionTimeOutRunnable);
        }
    }

    private long getBackgroundCenterDuration() {
        int selectedSize = getSelectedSize();
        int factor = selectedSize - 1;
        if (factor < 1) {
            factor = 1;
        }
        if (factor > 5) {
            factor = 5;
        }
        return DURATION_BACKGROUND_SINGLE_CHANGED * factor;
    }

    void startResultAnimation() {
        HSLog.d(TAG, "startResultAnimation");
//        if (isStopDialogShowing()) {
//            dismissStopDialog();
//        }
        startMemoryUsedDisappearAnimation();
        stopDotsAnimation();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HSLog.d(TAG, "startResultAnimation  attached == " + ResultPageActivity.isAttached());
                if (ResultPageActivity.isAttached()) {
                    dismissDialog();
                } else {
                    HSGlobalNotificationCenter.addObserver(ResultPageActivity.NOTIFICATION_RESULT_PAGE_ATTACHED, new INotificationObserver() {
                        @Override
                        public void onReceive(String s, HSBundle hsBundle) {
                            HSLog.d(TAG, "onReceive s == " + s);
                            HSGlobalNotificationCenter.removeObserver(this);
                            dismissDialog(); // Delay 500 ms
                        }
                    });
                }
            }
        }, 20 * FRAME);

        if (!mIsFlurryLogResultShow) {
            mIsFlurryLogResultShow = true;
        }
        mIsResultViewShow = true;
    }


    private void startCircleRotateAnimation() {
        mCircleInDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleInDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                mStartCircleAnimationTime = System.currentTimeMillis();
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleInIV, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleInIV.clearAnimation();
                mCircleInIV.setVisibility(View.INVISIBLE);
            }
        });
        mCircleInIV.clearAnimation();
        mCircleInIV.startAnimation(mCircleInDynamicRotateAnimation);

        mCircleMiddleDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleMiddleDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleMiddleIv, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleMiddleIv.clearAnimation();
                mCircleMiddleIv.setVisibility(View.INVISIBLE);
            }
        });
        mCircleMiddleIv.clearAnimation();
        mCircleMiddleIv.startAnimation(mCircleMiddleDynamicRotateAnimation);

        mCircleOutDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleOutDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleOutIv, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleOutIv.clearAnimation();
                mCircleOutIv.setVisibility(View.INVISIBLE);
            }
        });
        mCircleOutIv.clearAnimation();
        mCircleOutIv.startAnimation(mCircleOutDynamicRotateAnimation);
    }

    private int getAppTotalSizeMbs() {
        int totalMbs = 0;
        if (null != mSelectedAppList) {
            long totalBytes = 0;
            for (int i = 0; i < mSelectedAppList.size(); i++) {
                HSAppMemory hsAppMemory = mSelectedAppList.get(i);
                if (null != hsAppMemory) {
                    long hsAppMemoryBytes = hsAppMemory.getSize();
                    totalBytes += hsAppMemoryBytes;
                }
            }
            totalMbs = (int) (totalBytes / 1024 / 1024);
        }
        return totalMbs;
    }

    private long getCleanAppSize(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        }
        if (null != mSelectedAppList) {
            for (int i = 0; i < mSelectedAppList.size(); i++) {
                HSAppMemory hsAppMemory = mSelectedAppList.get(i);
                if (null != hsAppMemory && packageName.equals(hsAppMemory.getPackageName())) {
                    return hsAppMemory.getSize() / 1024 / 1024;
                }
            }
        }
        return 0;
    }

    private void startMemoryUsedAnimation(long duration, long startNumber, long endNumber) {
        HSLog.d(TAG, "startMemoryUsedAnimation " + duration + " " + startNumber + " " + endNumber);
        mBoostingTextLl.setVisibility(View.VISIBLE);
        mMemoryUsedNumberTv.startAnimation(duration, startNumber, endNumber);
    }

    private void startMemoryUsedDisappearAnimation() {
        Animation memoryUsedAlphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(
                DURATION_MEMORY_USED_ALPHA_REDUCE, START_OFF_CIRCLE_ROTATE_MAIN);
        mBoostingTextLl.setTag(true);
        LauncherAnimationUtils.startAnimation(mBoostingTextLl, memoryUsedAlphaDisAppearAnimation,
                new LauncherAnimationUtils.AnimationListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        super.onAnimationStart(animation);
                        // Use tag to record animation start status. When screen off then start animation in some phones,
                        // it will be onAnimationEnd callback before onAnimationStart.
                        Object tag = mBoostingTextLl.getTag();
                        if (null != tag && tag instanceof Boolean && (Boolean) tag) {
                            mBoostingTextLl.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mBoostingTextLl.setVisibility(View.INVISIBLE);
                        mBoostingTextLl.setTag(false);
                    }
                });
    }

    private void startClean(final int cleanType) {
        HSLog.d(TAG, "startClean cleanType = " + cleanType);
        if (null != mSelectedAppList && mSelectedAppList.size() > 0) {
            Collections.sort(mSelectedAppList, new Comparator<HSAppMemory>() {
                @Override
                public int compare(HSAppMemory o1, HSAppMemory o2) {
                    return (int) (o2.getSize() - o1.getSize());
                }
            });

            List<String> selectedPackageList = new ArrayList<>();
            for (HSAppMemory hsAppMemory : mSelectedAppList) {
                if (null != hsAppMemory) {
                    String packageName = hsAppMemory.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        selectedPackageList.add(packageName);
                    }
                }
            }
            final int totalSizeMbs = getAppTotalSizeMbs();
            mCurrentLastCleanSize = totalSizeMbs;

            //noinspection StatementWithEmptyBody
            if (cleanType == CLEAN_TYPE_NON_ROOT) {
                // No operation
            } else {
                HSLog.d(TAG, "startClean root or normal ***** startClean ***** cleanType = " + cleanType);
                int total = mSelectedAppList.size();
                startNormalCleanImgCurveAnimation(0, total, totalSizeMbs);

                HSAppMemoryManager.getInstance().startClean(mSelectedAppList,  new HSAppMemoryManager.MemoryTaskListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onProgressUpdated(int processedCount, int total, HSAppMemory hsAppMemory) {
                    }

                    @Override
                    public void onSucceeded(List<HSAppMemory> list, long size) {
                    }

                    @Override
                    public void onFailed(int code, String failMsg) {
                    }
                });
            }
        }
    }

    // Region Clean Task Handling Methods
    void onCleanStarted(long totalSizeMB, boolean shouldStartImgCurveAnimation) {
        if (mSelectedAppList != null && !mSelectedAppList.isEmpty()) {
            HSAppMemory hSAppMemory = mSelectedAppList.get(0);
            if (null != hSAppMemory) {
                String startPackageName = hSAppMemory.getPackageName();
                long endNumber = (mSelectedAppList.size() == 1) ? 0 : (totalSizeMB - getCleanAppSize(startPackageName));
                boolean isEnd = false;
                if (endNumber == 0) {
                    endNumber = mCurrentLastCleanSize / END_SIZE_LAST_PERCENT;
                    isEnd = true;
                }
                startMemoryUsedAnimation(isEnd ?
                                DURATION_END_SIZE_DECELERATE_FACTOR * DURATION_CLEAN_ITEM_APP
                                : DURATION_CLEAN_ITEM_APP,
                        totalSizeMB, endNumber);
                if (shouldStartImgCurveAnimation) {
                    startImgCurveAnimation(BoostAnimationManager.Boost.ICON_ONE, startPackageName);
                }
                mCurrentLastCleanSize = endNumber;
            }
        }
    }


    void onCleanProgressUpdated(int processedCount, int total, String packageName, boolean shouldStartImgCurveAnimation) {
//        startBackgroundChangedAnimation(ContextCompat.getColor(getContext(), R.color.boost_plus_yellow),
//                ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green), getBackgroundCenterDuration());
        startBackgroundAnimation(CleanAnimation.SECOND);

        String animationPackageName = "";
        if (0 <= processedCount && processedCount < getSelectedSize()) {
            HSAppMemory hSAppMemory = mSelectedAppList.get(processedCount);
            if (null != hSAppMemory) {
                animationPackageName = hSAppMemory.getPackageName();
            }
        }
        HSLog.d(TAG, "onProgressUpdated Clean progressCount = " + processedCount
                + " total = " + total + " packageName = " + packageName + " animationPackageName = " + animationPackageName);

        if (!TextUtils.isEmpty(animationPackageName) || processedCount < total) {
            long endNumber = (processedCount == total - 1) ? 0 : (mCurrentLastCleanSize - getCleanAppSize(animationPackageName));
            boolean isEnd = false;
            if (endNumber == 0) {
                endNumber = mCurrentLastCleanSize / END_SIZE_LAST_PERCENT;
                isEnd = true;
            }
            startMemoryUsedAnimation(isEnd ?
                            DURATION_END_SIZE_DECELERATE_FACTOR * DURATION_CLEAN_ITEM_APP
                            : DURATION_CLEAN_ITEM_APP,
                    mCurrentLastCleanSize, endNumber);
            if (shouldStartImgCurveAnimation) {
                startImgCurveAnimation(processedCount, animationPackageName);
            }
            mCurrentLastCleanSize = endNumber;
        }
    }

    void onCleanSucceeded(int cleanType) {
        HSLog.d(TAG, "onSucceeded cleanType = " + cleanType + " mIsStartForceStopCancel = "
                + mIsStartForceStopCancel + " mIsStartRootCancel = " + mIsStartRootCancel);
        mCleanResult = BoostPlusCleanDialog.CleanResult.CLEAN_SUCCESS;

        if (cleanType == CLEAN_TYPE_NON_ROOT && mIsStartForceStopCancel
                || (cleanType == CLEAN_TYPE_ROOT && mIsStartRootCancel)) {
            HSLog.d(TAG, "onSucceeded ****** force stop ****** onSucceeded onCancelExitClean");
            onCancelExitClean(true, mSelectedAppList);
        } else {
            startDecelerateAndGetPermission(cleanType);
        }
    }

    void onCleanFailed(int cleanType, int code, String failMsg) {
        HSLog.d(TAG, "onCleanFailed cleanType = " + cleanType + " code = "
                + code + " failMsg = " + failMsg + " mIsStartRootCancel = " + mIsStartRootCancel);
        mCleanResult = BoostPlusCleanDialog.CleanResult.CLEAN_FAILED;

        if ((cleanType == CLEAN_TYPE_ROOT && code == HSAppMemoryManager.FAIL_CANCEL)
                || (cleanType == CLEAN_TYPE_ROOT && mIsStartRootCancel)) {
            HSLog.d(TAG, "onCleanFailed ****** onCancelExitClean");
            onCancelExitClean(true, getCleanRemainingAppList());
        } else {
            startDecelerateAndGetPermission(cleanType);
        }
    }

    private List<HSAppMemory> getCleanRemainingAppList() {
        if (null == mSelectedAppList) {
            return null;
        }

        List<HSAppMemory> hsAppMemoryList = new ArrayList<>();
        hsAppMemoryList.addAll(mSelectedAppList);
        if (mCleanedAppPackageNameList.size() > 0) {
            for (String packageName : mCleanedAppPackageNameList) {
                if (!TextUtils.isEmpty(packageName)) {
                    for (HSAppMemory hsAppMemory : mSelectedAppList) {
                        if (null != hsAppMemory && hsAppMemory.getPackageName().equals(packageName)) {
                            hsAppMemoryList.remove(hsAppMemory);
                        }
                    }
                }
            }
        }
        return hsAppMemoryList;
    }

    public boolean isCleanResultViewShow() {
        return mIsResultViewShow;
    }

    private void startNormalCleanImgCurveAnimation(final int processCount, final int total, final long totalSizeMbs) {
        HSLog.d(TAG, "startNormalCleanImgCurveAnimation ***** processCount = " + processCount + " total = " + total + " totalSizeMbs = " + totalSizeMbs);
        if (null == mSelectedAppList) {
            return;
        }

        if (processCount >= mSelectedAppList.size()) {
            return;
        }

        HSAppMemory hsAppMemory = mSelectedAppList.get(processCount);
        boolean isCleanEnd = (processCount == total - 1);

        if (null != hsAppMemory) {
            String packageName = hsAppMemory.getPackageName();
            if (!TextUtils.isEmpty(packageName)) {
                if (processCount == 0) {
                    HSLog.d(TAG, "startClean root onStarted totalSizeMbs = " + totalSizeMbs);
                    mIsRootCleaning = true;
                    mCleanedAppPackageNameList.clear();
                    onCleanStarted(totalSizeMbs, false);
                }

                HSLog.d(TAG, "startClean root onProgressUpdated processedCount = " + processCount + " total = " + total);
                mCleanedAppPackageNameList.add(packageName);
                onCleanProgressUpdated(processCount + 1, total, packageName, false);
                startImgCurveAnimation(processCount + 1, packageName);
            }

            if (isCleanEnd) {
                HSLog.d(TAG, "startClean root onSucceeded");
                mCleanedAppPackageNameList.add(packageName);
                onCleanProgressUpdated(processCount + 1, total, packageName, false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsRootCleaning = false;
                        cancelCleanTimeOut();
                        onCleanSucceeded(CLEAN_TYPE_ROOT);
                    }
                }, DELAY_IMG_CURVE_ANIMATION * 4);
                return;
            }
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNormalCleanImgCurveAnimation(processCount + 1, total, totalSizeMbs);
            }
        }, DELAY_IMG_CURVE_ANIMATION + (long) (Math.random() * DELAY_IMG_CURVE_ANIMATION));
    }

    private void startDecelerateAndGetPermission(int cleanType) {
        dismissExitingDialog();
        boolean isRoot = (cleanType == CLEAN_TYPE_ROOT);

        if (isRoot) {
            startDecelerateResultAnimation();
            return;
        }

        startDecelerateResultAnimation();
    }

    private void startImgCurveAnimation(int processIndex, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        HSLog.d(TAG, "startImgCurveAnimation processIndex = " + processIndex + " packageName = " + packageName);

        Drawable currentDrawable = Utils.getAppIcon(packageName);

        int animationIndex;
        if (processIndex < 0) {
            animationIndex = 0;
        } else if (processIndex < BoostAnimationManager.COUNT_ICON) {
            animationIndex = processIndex;
        } else {
            animationIndex = processIndex % BoostAnimationManager.COUNT_ICON;
        }

        int[] location = new int[2];
        mBoostCenterIv.getLocationOnScreen(location);
        float endX = location[0];
        float endY = location[1];
        BoostAnimationManager boostAnimationManager = new BoostAnimationManager(endX, endY);

        if (null != currentDrawable) {
            ImageView animationIv = getCleanImageView(animationIndex);
            if (null != animationIv) {
                animationIv.setImageDrawable(currentDrawable);
                boostAnimationManager.startIconAnimation(animationIv, animationIndex);
            }
        }
    }

    private ImageView getCleanImageView(int index) {
        switch (index) {
            case BoostAnimationManager.Boost.ICON_ONE:
                return mIconOneV;
            case BoostAnimationManager.Boost.ICON_TWO:
                return mIconTwoV;
            case BoostAnimationManager.Boost.ICON_THREE:
                return mIconThreeV;
            case BoostAnimationManager.Boost.ICON_FOUR:
                return mIconFourV;
            case BoostAnimationManager.Boost.ICON_FIVE:
                return mIconFiveV;
            case BoostAnimationManager.Boost.ICON_SIX:
                return mIconSixV;
            case BoostAnimationManager.Boost.ICON_SEVEN:
                return mIconSevenV;
            default:
                return mIconOneV;
        }
    }


    private void startDotsAnimation() {
        // dots
        mDotsAnimationCount = 0;
        for (int i = 0; i < DOTS_COUNT; i++) {
            mDotsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDotsAnimationCount++;
                    if (mCleanMainRl.getTag() != null && mCleanMainRl.getTag() instanceof Boolean && (Boolean) mCleanMainRl.getTag()) {
                        mDotsHandler.removeCallbacksAndMessages(null);
                        return;
                    }
                    startDotAnimation();
                    if (mDotsAnimationCount == DOTS_COUNT - 1) {
                        mDotsHandler.removeCallbacksAndMessages(null);
                        startDotsAnimation();
                    }
                }
            }, (i + 1) * 5 * FRAME_DOTS);
        }
    }

    private void stopDotsAnimation() {
        if (null != mCleanMainRl) {
            mCleanMainRl.setTag(true);
        }
    }

    @SuppressWarnings("RestrictedApi")
    private void startDotAnimation() {
        final AppCompatImageView dotView = new AppCompatImageView(getContext());
        dotView.setImageDrawable(AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), R.drawable.boost_plus_light_dot_svg));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.dot_normal_anchor_iv);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.dot_normal_anchor_iv);
        Random random = new Random();
        int radius = random.nextInt(Dimensions.pxFromDp(50)) + Dimensions.pxFromDp(100);
        double radians = random.nextDouble() * 2 * Math.PI;
        int leftMargin = (int) (radius * Math.sin(radians));
        int topMargin = (int) (radius * Math.cos(radians));
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        mBoostIconContainer.addView(dotView, params);

        ObjectAnimator dotAnimation = ObjectAnimator.ofPropertyValuesHolder(dotView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.2f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -leftMargin),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -topMargin));
        dotAnimation.setDuration(8 * FRAME_DOTS);
        dotAnimation.setInterpolator(new AccelerateInterpolator(2));
        dotAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBoostIconContainer.removeView(dotView);
            }
        });
        dotAnimation.start();
    }


    void startResultPageActivity() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (mType == CLEAN_TYPE_TOOLBAR) {
//                    ResultPageActivity.startForBoost(getContext(), getAppTotalSizeMbs(), true);
//                } else if (mType == CLEAN_TYPE_CLEAN_CENTER) {
//                    ResultPageActivity.startForBoost(getContext(), getAppTotalSizeMbs(), false);
//                } else {
//                    ResultPageActivity.startForBoostPlus((Activity) getContext(), getAppTotalSizeMbs(), true);
//                }
                HSLog.d(TAG, "startResultPageActivity ");
                ResultPageActivity.startForBoost(getContext(), getAppTotalSizeMbs());
            }
        }, 450L);
    }

    void startDecelerateResultAnimation() {
//        startBackgroundChangedAnimation(ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green),
//                ContextCompat.getColor(getContext(), R.color.boost_plus_clean_bg), DURATION_BACKGROUND_END_CHANGED);
        startBackgroundAnimation(CleanAnimation.THIRD);

        startMemoryUsedAnimation(DURATION_CLEAN_ITEM_APP_END_LAST, mCurrentLastCleanSize, 0);
        long startOffset;
        long duration = System.currentTimeMillis() - mStartCircleAnimationTime;
        if (duration >= DURATION_ROTATE_MAIN) {
            startOffset = 0;
        } else {
            startOffset = DURATION_ROTATE_MAIN - duration;
        }
        if (startOffset > 1400L) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startResultPageActivity();
                }
            }, startOffset - 1400L);
        } else {
            startOffset = 1400L;
            startResultPageActivity();
        }

        HSLog.d(TAG, "startDecelerateResultAnimation startOffset = " + startOffset);

        Runnable decelerateRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mCircleInDynamicRotateAnimation) {
                            mCircleInDynamicRotateAnimation.startDecelerateMode();
                        }
                        if (null != mCircleMiddleDynamicRotateAnimation) {
                            mCircleMiddleDynamicRotateAnimation.startDecelerateMode();
                        }
                        if (null != mCircleOutDynamicRotateAnimation) {
                            mCircleOutDynamicRotateAnimation.startDecelerateMode();
                        }

                        LauncherAnimUtils.startAlphaDisappearAnimation(mCircleInIV, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                        LauncherAnimUtils.startAlphaDisappearAnimation(mCircleMiddleIv, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                        LauncherAnimUtils.startAlphaDisappearAnimation(mCircleOutIv, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                    }
                });
            }
        };
        mHandler.postDelayed(decelerateRunnable, startOffset);

        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                startResultAnimation();
            }
        };
        mHandler.postDelayed(tickRunnable, startOffset);
    }

    private void startBackgroundAnimation(CleanAnimation type) {
        int colorFrom;
        int colorEnd;
        long duration;

        switch (type) {
            default:
            case FIRST:
                switch (ramStatus) {
                    default:
                    case EMERGENCY:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_red);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_yellow);
                        duration = DURATION_BACKGROUND_SINGLE_CHANGED;
                        break;
                    case NORMAL:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_yellow);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                        duration = DURATION_BACKGROUND_SINGLE_CHANGED * 2;
                        break;
                    case GOOD:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_bg);
                        duration = DURATION_BACKGROUND_SINGLE_CHANGED * 3;
                        break;
                }
                break;
            case SECOND:
                switch (ramStatus) {
                    default:
                    case EMERGENCY:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_yellow);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                        duration = getBackgroundCenterDuration();
                        break;
                    case NORMAL:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_yellow);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                        duration = getBackgroundCenterDuration();
                        break;
                    case GOOD:
                        colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                        colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_bg);
                        duration = getBackgroundCenterDuration() * 2;
                        break;
                }
                break;
            case THIRD:
                colorFrom = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green);
                colorEnd = ContextCompat.getColor(getContext(), R.color.boost_plus_clean_bg);
                duration = DURATION_BACKGROUND_END_CHANGED;
                break;
        }

        startBackgroundChangedAnimation(colorFrom, colorEnd, duration);
    }

    private void startBackgroundChangedAnimation(int colorFrom, int colorEnd, long duration) {
        if (null != mBgColorAnimator && mBgColorAnimator.isRunning()) {
            mBgColorAnimator.cancel();
        }
        int backgroundColor = ViewUtils.getBackgroundColor(mContainerV);
        if (backgroundColor != 0) {
            colorFrom = backgroundColor;
        }
        mBgColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorEnd);
        mBgColorAnimator.setDuration(duration);
        mBgColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mContainerV.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        mBgColorAnimator.start();
    }

    private void onBackClicked() {
        dismissDialog();
    }
}
