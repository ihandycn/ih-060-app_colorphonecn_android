package com.honeycomb.colorphone.recentapp;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.acb.utils.CommonUtils;
import com.acb.utils.ConcurrentUtils;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.AppInfo;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.lang.ref.WeakReference;
import java.util.List;

public class SmartAssistantView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = SmartAssistantView.class.getSimpleName();
    public static final String NOTIFICATION_FINISH = "recent_apps_ad_clicked";
    private static final boolean DEBUG_MODE = true & BuildConfig.DEBUG;
    private static final int MSG_ICON_SET = 1;

//    private AcbNativeAd mAd;
//    private AcbNativeAdContainerView mNativeContent;
//    private AcbNativeAdLoader mLoader;
    private AdLogger mAdLogger = new AdLogger(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);

    private RipplePopupView mMenuPopupView;
    private int leftShiftDistance = 0;

    private TableLayout mAppsContainerView;
    private int mAppWidth;
    private int mAppIconSize = Utils.pxFromDp(48);

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ICON_SET:
                    TextView textView = (TextView) msg.obj;
                    Drawable drawable = (Drawable) textView.getTag();
                    drawable.setBounds(0, 0, mAppIconSize, mAppIconSize);
                    textView.setCompoundDrawables(null, drawable, null, null);
                    break;
            }
        }
    };
    private AcbExpressAdView adView;
    private ViewGroup mAdContainer;

    public SmartAssistantView(Context context) {
        super(context);

        mAdLogger.adSessionStart();
//        List<AcbNativeAd> ads = AcbNativeAdManager.fetch(HSApplication.getContext(),
//                AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME, 1);
//        if (!DEBUG_MODE) {
//            if (ads.isEmpty()) {
//                HSLog.d(TAG, "should show with ad, but ad is null");
//                AcbNativeAdManager.preload(HSApplication.getContext(), 1, AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);
//                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH);
//                return;
//            }
//            mAd = ads.get(0);
//        }

        View.inflate(context, R.layout.smart_assistant, this);
        initView();
        bindRecentApps();
        SmartAssistantUtils.recordSmartAssistantShowTime();

        LauncherAnalytics.logEvent("Recent_Apps_Show");

        HSLog.d("RecentApps", "show recent apps");
    }

    private void initView() {
        findViewById(R.id.recent_app_close).setOnClickListener(this);
        findViewById(R.id.recent_app_menu).setOnClickListener(this);
        mAppsContainerView = findViewById(R.id.recent_apps_container_view);
        mAppWidth = (CommonUtils.getPhoneWidth(getContext()) - CommonUtils.pxFromDp(18 * 4)) / 4;

        TypefacedTextView appText = findViewById(R.id.app_name_text);
        if (SmartAssistantUtils.isFirstShowSmartAssistant()) {
            findViewById(R.id.robot_msg_text).setVisibility(VISIBLE);
            appText.setVisibility(GONE);
        } else {
            appText.setVisibility(VISIBLE);
        }

        mAdContainer = (ViewGroup) findViewById(R.id.ad_fragment);
    }

    @SuppressWarnings("RedundantCast")
    private void bindRecentApps() {
        final WeakReference<View> weakReference = new WeakReference<View>(this);
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final List<RecentAppInfo> apps = SmartAssistantUtils.getSmartAssistantApps();
                if (weakReference.get() != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                           doBindRecentApps(apps);
                        }
                    });
                }
            }
        });
    }

    private void doBindRecentApps(List<RecentAppInfo> apps) {
        for (int i = 0; i < apps.size(); i++) {
            RecentAppInfo appInfo = apps.get(i);
            addAppView(appInfo);
        }
        addAdView(getContext());
    }

    private void addAppView(final RecentAppInfo appInfo) {
        TextView textView = new TextView(getContext());
        textView.setOnClickListener(this);

        textView.setCompoundDrawablePadding(Utils.pxFromDp(2));
        textView.setTextColor(Color.BLACK);
        textView.setText(appInfo.getName());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
        textView.setTypeface(FontUtils.getTypeface(FontUtils.Font.ROBOTO_CONDENSED));
        textView.setSingleLine(true);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        loadAppIconInto(textView, appInfo.getAppInfo());

        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAnalytics.logEvent("Recent_Apps_AppIcon_Clicked", "Type", getAppIconClickEventType(appInfo));
                Utils.startActivitySafely(v.getContext(), appInfo.getIntent());
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH);
            }
        });

        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rowParams.topMargin = CommonUtils.pxFromDp(10);

        int count = mAppsContainerView.getChildCount();
        if (count > 0) {
            boolean isExist = true;
            TableRow tableRow = (TableRow) mAppsContainerView.getChildAt(count - 1);
            if (tableRow.getChildCount() >= 4) {
                isExist = false;
                tableRow = new TableRow(getContext());
            }
            tableRow.addView(textView, new TableRow.LayoutParams(mAppWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (!isExist) {
                mAppsContainerView.addView(tableRow, rowParams);
            }
        } else {
            TableRow tableRow = new TableRow(getContext());
            tableRow.addView(textView, new TableRow.LayoutParams(mAppWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            mAppsContainerView.addView(tableRow, rowParams);
        }
    }

    private String getAppIconClickEventType(RecentAppInfo appInfo) {
        String result = "None";
        switch (appInfo.getType()) {
            case RecentAppInfo.TYPE_MOSTLY_USED:
                result = "MostUse";
                break;
            case RecentAppInfo.TYPE_NEW_INSTALL:
                result = "New";
                break;
            case RecentAppInfo.TYPE_RECENTLY_USED:
                result = "RecentUse";
                break;
        }
        return result;
    }

    private void loadAppIconInto(TextView textView, final AppInfo appInfo) {
        final WeakReference<TextView> weakReference = new WeakReference<TextView>(textView);
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = appInfo.getIcon();
                TextView tv = weakReference.get();
                if (tv != null) {
                    Message msg = Message.obtain();
                    msg.what = MSG_ICON_SET;
                    msg.obj = tv;
                    tv.setTag(drawable);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

//    private void bindAd() {
//        if (mAd != null) {
//            initAdView();
//            mNativeContent.fillNativeAd(mAd);
//            mAd.setNativeClickListener(SmartAssistantView.this);
//            mAdLogger.adShow();
//            LauncherAnalytics.logEvent("Recent_Apps_Ad_Show");
//        }
//    }

    private void addAdView(Context context) {

        if (adView == null) {
            adView = new AcbExpressAdView(context, AdPlacements.AD_MSG_NEW);
            AcbExpressAdView.CustomLayout layout = new AcbExpressAdView.CustomLayout(R.layout.ad_view);
            layout.setActionId(R.id.recent_app_action_btn);
            layout.setChoiceId(R.id.recent_app_ad_choice_icon);
            layout.setTitleId(R.id.recent_app_ad_title);
            layout.setContentId(R.id.recent_app_ad_description);
            layout.setIconId(R.id.recent_app_icon);
            layout.setPrimaryId(R.id.recent_app_banner);

            adView.setCustomLayout(layout);
            adView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_All);
            adView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                @Override
                public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                    LauncherAnalytics.logEvent("Recent_Apps_Ad_Clicked");
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH);
                }

                @Override
                public void onAdShown(AcbExpressAdView acbExpressAdView) {
                    mAdLogger.adShow();
                    LauncherAnalytics.logEvent("Recent_Apps_Ad_Show");
                }
            });

            adView.prepareAd(new AcbExpressAdView.PrepareAdListener() {
                @Override
                public void onAdReady(AcbExpressAdView acbExpressAdView) {
                    mAdContainer.setVisibility(VISIBLE);
                    adView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    mAdContainer.addView(acbExpressAdView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                }

                @Override
                public void onPrepareAdFailed(AcbExpressAdView acbExpressAdView) {

                }
            });
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.recent_app_close:

                break;
            case R.id.recent_app_menu:
                LauncherAnalytics.logEvent("RecentApps_Disable_Clicked");
                showMenuPopupWindow(getContext(), v);
                return;
        }
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH);
    }

    public void onAddedToWindow() {
        setAlpha(0.0f);
        animate().alpha(1)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public void dismiss(boolean option, boolean animated) {
        removeAds();
        mAdLogger.adSessionEnd();
        mHandler.removeCallbacksAndMessages(null);
        // According to using activity, we do not need to remove recent app guide any more
        //LauncherFloatWindowManager.getInstance().removeNormalGuide();
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (mMenuPopupView == null) {
            mMenuPopupView = new RipplePopupView(context, this);
            View popContent = LayoutInflater.from(new android.view.ContextThemeWrapper(context,
                    R.style.ChargingScreenTransparentTheme)).inflate(R.layout.smart_assistant_menu,
                    this, false);
            final TextView disableGuide = (TextView) popContent.findViewById(R.id.tv_close);
            disableGuide.requestLayout();
            if (disableGuide.getWidth() <= 0) {
                disableGuide.measure(0, 0);
                leftShiftDistance = disableGuide.getMeasuredWidth();
                disableGuide.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        if (disableGuide.getWidth() <= 0) {
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            disableGuide.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            disableGuide.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        leftShiftDistance = disableGuide.getWidth();
                    }
                });
            } else {
                leftShiftDistance = disableGuide.getWidth();
            }

            disableGuide.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ChargingScreenUtils.isFastDoubleClick()) {
                        return;
                    }
                    LauncherAnalytics.logEvent("RecentApps_Disable_Success");
                    mMenuPopupView.dismiss();
                    SmartAssistantUtils.disableByUser();
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH);
                }
            });

            mMenuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
            mMenuPopupView.setContentView(popContent);
            mMenuPopupView.setOutSideClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMenuPopupView.dismiss();
                }
            });
        }

        mMenuPopupView.showAsDropDown(anchorView,
                -(leftShiftDistance - anchorView.getWidth()),
                -(CommonUtils.pxFromDp(80)
                        + anchorView.getHeight()) / 2);
    }

    private void removeAds() {
        if (mAdContainer != null) {
            mAdContainer.removeAllViews();
            mAdContainer = null;
        }

        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

}
