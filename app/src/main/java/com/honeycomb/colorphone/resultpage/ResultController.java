package com.honeycomb.colorphone.resultpage;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ClipDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.lockscreen.chargingscreen.view.FlashButton;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.LauncherAnimationUtils;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.RippleUtils;
import com.honeycomb.colorphone.util.Thunk;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;
import net.appcloudbox.common.ImageLoader.AcbImageLoader;
import net.appcloudbox.common.ImageLoader.AcbImageLoaderListener;
import net.appcloudbox.common.utils.AcbError;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public abstract class ResultController implements View.OnClickListener {

    protected static final String TAG = "ResultController";
    static final long FRAME = 100;
    static final long FRAME_HALF = 50;
    private static final int FRAME_40 = 40;

    private static final long START_DELAY_CARDS = FRAME / 10 * 86;
    @Thunk
    static final long DURATION_CARD_TRANSLATE = 440;

    @Thunk
    static final long DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY = 0;
    @Thunk
    static final long DURATION_CARD_TRANSLATE_DELAY = 0;

    @Thunk
    static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 360;

    // Ad / charging defaultViewScreen
    private static final long START_DELAY_AD_OR_FUNCTION = 16 * FRAME;

    protected ResultPageActivity mActivity;
    int mScreenHeight;
    int mResultType;
    Type mType = Type.AD;

    private FrameLayout mTransitionView;
    private FrameLayout mAdOrFunctionContainerView;
    private RecyclerView mCardRecyclerView;
    @Thunk View mResultView;
    private FrameLayout adOrFunctionView;
    private View mBgView;
    private View mHeaderTagView;
    private RelativeLayout mPageRootView;

    // Ad or charging defaultViewScreen
    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private ImageView mImageIv;
    private ViewGroup mAdChoice;
    private ImageView mAdIconView;
    private TextView mTitleTv;
    private TextView mDescriptionTv;
    private FlashButton mActionBtn;
    private View primaryViewContainer;
    private View bottomContainer;
    private View iconContainer;
    private View coverShader;

    private List<CardData> mCardDataList;

    public enum Type {
        AD,
//        CHARGE_SCREEN,
//        NOTIFICATION_CLEANER,
//        CARD_VIEW,
//        APP_LOCK,
        DEFAULT_VIEW,
//        UNREAD_MESSAGE,
//        WHATS_APP,
    }

    public Interpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.26f, 1f, 0.48f, 1f);

    //notification cleaner
//    private AnimatedHorizontalIcons animatedHorizontalIcons;
//    private AnimatedNotificationHeader animatedNotificationHeader;
//    private AnimatedNotificationGroup animateNotificationGroup;
//    private AnimatedShield animatedShield;

    private View animatedRootView;
    private View animatedPhoneFrameView;
    private ImageView phoneBackgroundImageView;
    private LinearLayout animationContainerLayout;

    //default view
    private View defaultViewPhone;
    private View defaultViewScreen;
    private View defaultViewStar1;
    private View defaultViewStar2;
    private View defaultViewStar3;
    private View defaultViewDescription;
    private ImageView defaultViewShield;
    private TextView defaultViewBtnOk;
    private ClipDrawable defaultViewTickDrawable;
    private CardOptimizedFlashView defaultViewFlashView;

    protected AcbInterstitialAd mInterstitialAd;

    @SuppressLint("HandlerLeak") // This handler holds activity reference for no longer than 120s
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_WHAT_NOTIFICATION_LISTENING_CHECK:
//                    HSLog.d(TAG, "MSG_WHAT_NOTIFICATION_LISTENING_CHECK");
//                    if (!NotificationCleanerUtils.isNotificationAccessGranted(mActivity)) {
//                        HSLog.d(TAG, "MSG_WHAT_NOTIFICATION_LISTENING_CHECK, continue check");
//                        sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, INTERVAL_PERMISSION_CHECK);
//                        break;
//                    }
//                    switch (mType) {
//                        case NOTIFICATION_CLEANER:
//                            NotificationManager.getInstance().cancel(NotificationCondition.NOTIFICATION_ID_NOTIFICATION_CLEANER);
//                            Intent intentSelf = new Intent(HSApplication.getContext(), NotificationGuideActivity.class);
//                            intentSelf.putExtra(NotificationCleanerUtils.EXTRA_IS_AUTHORIZATION_SUCCESS, true);
//                            intentSelf.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
//                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
//                            HSApplication.getContext().startActivity(intentSelf);
//                            AutoPilotUtils.logNCAccessOpenSuccess();
//                            LauncherAnalytics.logEvent("NotificationAccess_Grant_Success", "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//                            break;
//                        case UNREAD_MESSAGE:
//                            LauncherAnalytics.logEvent("NotificationAccess_Grant_Success", "type", ResultConstants.UNREAD_MESSGAE);
//                            Intent badgeSettingsIntent = new Intent(HSApplication.getContext(), BadgeSettingsActivity.class);
//                            badgeSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            HSApplication.getContext().startActivity(badgeSettingsIntent);
//                            break;
//                        case WHATS_APP:
//                            LauncherAnalytics.logEvent("NotificationAccess_Grant_Success", "type", ResultConstants.WHATS_APP);
//                            mActivity.finishAndNotify();
//                            break;
//                        default:
//                            break;
//                    }
//                    if (Utils.isNewUser() && !NotificationCleanerUtils.isNotificationCleanerSettingsEverSwitched()) {
//                        NotificationCleanerProvider.switchNotificationOrganizer(true);
//                    }
//                    LauncherFloatWindowManager.getInstance().removeFloatButton();
//                    LauncherFloatWindowManager.getInstance().removePermissionGuide(false);
//                    mActivity.finishSelfAndParentActivity();
//                    break;
//                case MSG_WHAT_NOTIFICATION_LISTENING_CANCEL:
//                    HSLog.d(TAG, "MSG_WHAT_NOTIFICATION_LISTENING_CANCEL");
//                    removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
//                    break;
//                default:
//                    break;
//            }
        }
    };

    ResultController() {
    }

    protected void init(ResultPageActivity activity, int resultType, Type type, @Nullable AcbInterstitialAd interstitialAd, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        HSLog.d(TAG, "ResultController init *** resultType = " + resultType + " type = " + type);
        mActivity = activity;
        mType = type;
        logViewEvent(type);

        mCardDataList = cardDataList;
        mResultType = resultType;
        mScreenHeight = Utils.getPhoneHeight(activity);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);

        mPageRootView = ViewUtils.findViewById(activity, R.id.page_root_view);
        mPageRootView.post(new Runnable() {
            @Override public void run() {
                int height = mPageRootView.getHeight();
                if (mScreenHeight - height == Utils.getNavigationBarHeight(mActivity)) {
                    mScreenHeight = height;
                }
            }
        });
        mBgView = ViewUtils.findViewById(activity, R.id.bg_view);
        setBgViewSize();

        mHeaderTagView = ViewUtils.findViewById(activity, R.id.result_header_tag_view);

        mTransitionView = ViewUtils.findViewById(activity, R.id.transition_view_container);
        if (null != mTransitionView) {
            mTransitionView.removeAllViews();
            layoutInflater.inflate(getLayoutId(), mTransitionView, true);
            onFinishInflateTransitionView(mTransitionView);
        }
        mInterstitialAd = interstitialAd;

        switch (type) {
            case AD:
//            case CHARGE_SCREEN:
//            case NOTIFICATION_CLEANER:
//            case APP_LOCK:
            case DEFAULT_VIEW:
//            case UNREAD_MESSAGE:
//            case WHATS_APP:
                initAdOrFunctionView(activity, layoutInflater, ad);
                break;
//            case CARD_VIEW:
//                initCardView(activity, ad);
//                break;
        }
        mResultView = ViewUtils.findViewById(activity, R.id.result_view);
    }

    private void initAdOrFunctionView(Activity activity, LayoutInflater layoutInflater, AcbNativeAd ad) {
        HSLog.d(TAG, "initAdOrFunctionView");
        adOrFunctionView = ViewUtils.findViewById(activity, R.id.ad_or_function_view_container);
        if (null != adOrFunctionView) {
            int layoutId = getAdOrFunctionViewLayoutId();
            View resultContentView = layoutInflater.inflate(layoutId, mAdOrFunctionContainerView, false);
            mAdOrFunctionContainerView = adOrFunctionView;
            onFinishInflateResultView(resultContentView, ad);
            initActionButton(activity);
            mAdOrFunctionContainerView.setVisibility(View.VISIBLE);
            if (null != mCardRecyclerView) mCardRecyclerView.setVisibility(View.GONE);
        }
    }

    private int getAdOrFunctionViewLayoutId() {
//        if (mType == Type.APP_LOCK) {
//            return R.layout.result_page_fullscreen_applock;
//        }
        if (mType == Type.DEFAULT_VIEW) {
            return R.layout.result_page_default_view;
        }
        return R.layout.result_page_fullscreen;
    }

//    private void initCardView(Activity activity, AcbNativeAd ad) {
//        HSLog.d(TAG, "initCardView");
//        mCardRecyclerView = ViewUtils.findViewById(activity, R.id.result_card_recycler_view);
//        mCardRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
//        mCardRecyclerView.setHasFixedSize(true);
//        ResultListAdapter resultListAdapter = new ResultListAdapter(mActivity, mResultType, mCardDataList, ad);
//        mCardRecyclerView.setAdapter(resultListAdapter);
//        initActionButton(activity);
//        mCardRecyclerView.setVisibility(View.VISIBLE);
//
//        if (null != mAdOrFunctionContainerView) {
//            mAdOrFunctionContainerView.setVisibility(View.GONE);
//        }
//    }

//    private void initNotificationCleanerView() {
//        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
//        View resultContentView = layoutInflater.inflate(R.layout.result_page_notification_cleaner_animation_view, mAdOrFunctionContainerView, false);
//
//        animatedRootView = resultContentView.findViewById(R.id.container_view);
//        animatedRootView.setVisibility(View.INVISIBLE);
//        animatedPhoneFrameView = resultContentView.findViewById(R.id.animated_notification_phone_frame);
//
//        if (mActivity.getResources().getDisplayMetrics().densityDpi <= DisplayMetrics.DENSITY_HIGH) {
//            animatedPhoneFrameView.setScaleX(0.9f);
//            animatedPhoneFrameView.setScaleY(0.9f);
//        }
//
//        phoneBackgroundImageView = resultContentView.findViewById(R.id.animated_notification_phone_background);
//        animationContainerLayout = resultContentView.findViewById(R.id.animated_notification_container);
//        animatedHorizontalIcons = resultContentView.findViewById(R.id.horizontal_icons);
//        animatedShield = resultContentView.findViewById(R.id.animated_shield);
//        animatedShield.setMovePostionRatio(0.88f);
//        animateNotificationGroup = resultContentView.findViewById(R.id.expand_notification_group);
//        animatedNotificationHeader = resultContentView.findViewById(R.id.shrink_drawer_notification_header);
//
//        resultContentView.findViewById(R.id.promote_charging_button).setOnClickListener(this);
//
//        phoneBackgroundImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    phoneBackgroundImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                } else {
//                    phoneBackgroundImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//
//                int phoneFrameWidth = phoneBackgroundImageView.getWidth();
//                int phoneFrameHeight = phoneBackgroundImageView.getHeight();
//
//                float containerLeftStart = phoneFrameWidth * RATIO_ANIMATION_CONTAINER_LEFT_START;
//                float containerTopStart = phoneFrameHeight * RATIO_ANIMATION_CONTAINER_TOP_START;
//                int containerWidth = (int) (phoneFrameWidth * RATIO_ANIMATION_CONTAINER_WIDTH);
//                int containerHeight = (int) (phoneFrameHeight * RATIO_ANIMATION_CONTAINER_HEIGHT);
//
//                RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(containerWidth, containerHeight);
//                animationContainerLayout.setX((Utils.isRtl() ? -1 : 1) * containerLeftStart);
//                animationContainerLayout.setY(containerTopStart);
//                animationContainerLayout.setLayoutParams(containerParams);
//
//                RelativeLayout.LayoutParams shieldParams = (RelativeLayout.LayoutParams) animatedShield.getLayoutParams();
//                shieldParams.width = (int) (phoneFrameWidth * RATIO_ANIMATED_SHIELD_WIDTH);
//                shieldParams.height = (int) (phoneFrameHeight * RATIO_ANIMATED_SHIELD_HEIGHT);
//                animatedShield.setLayoutParams(shieldParams);
//            }
//        });
//        mAdOrFunctionContainerView.addView(resultContentView);
//    }

//    private void initNotificationCleanerCallbacks() {
//
//        animatedNotificationHeader.setOnHeaderAnimationFinishListener(new AnimatedNotificationHeader.OnHeaderAnimationFinishListener() {
//            @Override
//            public void onLastItemCollapsed() {
//                animateNotificationGroup.collapseStayItems();
//                animatedHorizontalIcons.postDelayed(new Runnable() {
//                    @Override public void run() {
//                        return animatedHorizontalIcons.collapseHorizontalIcons();
//                    }
//                }, DELAY_HORIZONTAL_ICONS_COLLAPSE);
//            }
//
//            @Override
//            public void onHeaderAnimated() {
//
//            }
//        });
//
//        animateNotificationGroup.setOnAnimationFinishListener(new AnimatedNotificationGroup.OnAnimationFinishListener() {
//            @Override
//            public void onExpandFinish() {
//                handler.postDelayed(new Runnable() {
//                    @Override public void run() {
//                        animatedShield.enlargeAndRotateAnimation();
//                    }
//                }, DELAY_SHIELD_APPEAR);
//            }
//
//            @Override
//            public void onStayItemCollapseFinish() {
////                startActivateTipAnimation();
//            }
//        });
//
//        animatedShield.setOnAnimationFinishListener(() -> {
//            animatedNotificationHeader.setVisibility(View.VISIBLE);
//            animateNotificationGroup.collapseNotificationItems(animatedNotificationHeader);
//        });
//    }

//    private void startNotificationCleanerAnimations() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                animatedRootView.setVisibility(View.VISIBLE);
//
//                startCardTranslationAnimation(animatedRootView, new LauncherAnimationUtils.AnimationListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        super.onAnimationEnd(animation);
//                        animatedHorizontalIcons.expandHorizontalIcons();
//                        animateNotificationGroup.expandNotificationItems();
//                    }
//                });
//            }
//        }, DELAY_ANIMATION_START);
//    }

    private void initActionButton(Context context) {
        if (null != mActionBtn) {
            mActionBtn.setBackgroundDrawable(RippleUtils.createRippleDrawable(mActivity.getBackgroundColor(), 2));
        }
    }

//    private void initUnreadMessageView() {
//        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
//        View resultContentView = layoutInflater.inflate(R.layout.result_page_fullscreen_unread_message, mAdOrFunctionContainerView, false);
//
//        mActionBtn = ViewUtils.findViewById(resultContentView, R.id.promote_charging_button);
//        mActionBtn.setRepeatCount(10);
//        mActionBtn.setOnClickListener(this);
//        mAdOrFunctionContainerView.addView(resultContentView);
//    }
//
//    private void initWhatsAppView() {
//        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
//        View resultContentView = layoutInflater.inflate(R.layout.result_page_fullscreen_whats_app, mAdOrFunctionContainerView, false);
//
//        mActionBtn = ViewUtils.findViewById(resultContentView, R.id.promote_charging_button);
//        mActionBtn.setRepeatCount(10);
//        mActionBtn.setOnClickListener(this);
//        mAdOrFunctionContainerView.addView(resultContentView);
//    }


    protected abstract int getLayoutId();

    protected abstract void onFinishInflateTransitionView(View transitionView);

    @SuppressWarnings("RestrictedApi")
    protected void onFinishInflateResultView(View resultView, final AcbNativeAd ad) {
        HSLog.d(TAG, "onFinishInflateResultView mType = " + mType);
        final Context context = getContext();

        if (mType == Type.DEFAULT_VIEW) {
            defaultViewPhone = ViewUtils.findViewById(resultView, R.id.phone);
            defaultViewScreen = ViewUtils.findViewById(resultView, R.id.screen);
            defaultViewStar1 = ViewUtils.findViewById(resultView, R.id.star_1);
            defaultViewStar2 = ViewUtils.findViewById(resultView, R.id.star_2);
            defaultViewStar3 = ViewUtils.findViewById(resultView, R.id.star_3);
            defaultViewShield = ViewUtils.findViewById(resultView, R.id.shield);
            ImageView tick = ViewUtils.findViewById(resultView, R.id.tick);
            defaultViewTickDrawable = (ClipDrawable) tick.getDrawable();
            defaultViewDescription = ViewUtils.findViewById(resultView, R.id.description);
            defaultViewBtnOk = ViewUtils.findViewById(resultView, R.id.btn_ok);
            defaultViewFlashView = ViewUtils.findViewById(resultView, R.id.flash_view);

//            switch (mResultType) {
//                case ResultConstants.RESULT_TYPE_BOOST_PLUS:
//                case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_boost);
//                    break;
//                case ResultConstants.RESULT_TYPE_BATTERY:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_battery);
//                    break;
//                case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_junk_cleaner);
//                    break;
//                case ResultConstants.RESULT_TYPE_CPU_COOLER:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_cpu);
//                    break;
//                case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_notification_cleaner);
//                    break;
//                case ResultConstants.RESULT_TYPE_VIRUS_SCAN:
//                    defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_shield_default);
//                    break;
//                default:
//                    break;
//            }
            defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_boost);

            defaultViewBtnOk.setTextColor(((ResultPageActivity) context).getBackgroundColor());
            defaultViewBtnOk.setBackgroundDrawable(RippleUtils.createRippleDrawable(0xffffffff, 0xffeeeeee, 2));
            defaultViewBtnOk.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ((ResultPageActivity) context).finishAndNotify();
                }
            });

            defaultViewPhone.setTranslationY(Utils.pxFromDp(77));
            defaultViewPhone.setAlpha(0);
            defaultViewScreen.setTranslationY(Utils.pxFromDp(77));
            defaultViewScreen.setAlpha(0);
            defaultViewDescription.setAlpha(0);
            defaultViewBtnOk.setScaleX(0);
            defaultViewBtnOk.setAlpha(0);
            defaultViewTickDrawable.setLevel(0);
            defaultViewStar1.setAlpha(0);
            defaultViewStar2.setAlpha(0);
            defaultViewStar3.setAlpha(0);

            mAdOrFunctionContainerView.addView(resultView);
            return;
        }
//
//        if (mType == Type.APP_LOCK) {
//            mActionBtn = resultView.findViewById(R.id.promote_charging_button);
//            mActionBtn.setRepeatCount(10);
//            mActionBtn.setOnClickListener(this);
//
//            mAdOrFunctionContainerView.addView(resultView);
//            return;
//        }

//        if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER) {
        if (mType == Type.AD) {
            mAdImageContainer = ViewUtils.findViewById(resultView, R.id.result_image_container_ad);
            mImageIv = ViewUtils.findViewById(resultView, R.id.result_charging_image);
            mAdChoice = ViewUtils.findViewById(resultView, R.id.result_ad_choice);
            mAdIconView = ViewUtils.findViewById(resultView, R.id.result_ad_icon);
            mTitleTv = ViewUtils.findViewById(resultView, R.id.promote_charging_title);
            mDescriptionTv = ViewUtils.findViewById(resultView, R.id.promote_charging_content);
            mActionBtn = ViewUtils.findViewById(resultView, R.id.promote_charging_button);
            mActionBtn.setRepeatCount(10);
            primaryViewContainer = ViewUtils.findViewById(resultView, R.id.promote_charging_content_top_container);
            bottomContainer = ViewUtils.findViewById(resultView, R.id.promote_charging_bottom_container);
            iconContainer = ViewUtils.findViewById(resultView, R.id.promote_charging_icon_container);
            coverShader = ViewUtils.findViewById(resultView, R.id.cover_icon);
            coverShader.setBackgroundColor(mActivity.getBackgroundColor());

            mAdImageContainer.setBitmapConfig(Bitmap.Config.RGB_565);
            int targetWidth = Utils.getPhoneWidth(context) - 2 * Utils.pxFromDp(27) - 2 * Utils.pxFromDp(20);
            int targetHeight = (int) (targetWidth / 1.9f);
            mAdImageContainer.setTargetSizePX(targetWidth, targetHeight);
        }
        switch (mType) {
            case AD:
                AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
                adContainer.addContentView(resultView);
                adContainer.setAdTitleView(mTitleTv);
                adContainer.setAdBodyView(mDescriptionTv);
                adContainer.setAdPrimaryView(mAdImageContainer);
                adContainer.setAdChoiceView(mAdChoice);
                adContainer.setAdActionView(mActionBtn);

                mAdImageContainer.setVisibility(View.VISIBLE);
                final boolean valid = new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getIconUrl(),
                        ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_ICON), new AcbImageLoaderListener() {
                            @Override
                            public void imageLoaded(Bitmap bitmap) {
                                mAdIconView.setImageBitmap(bitmap);
                            }

                            @Override
                            public void imageFailed(AcbError acbError) {
                                new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getImageUrl(),
                                        ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_IMAGE), new AcbImageLoaderListener() {
                                            @Override
                                            public void imageLoaded(Bitmap bitmap) {
                                                Bitmap newBitmap;
                                                try {
                                                    newBitmap = createCenterCropBitmap(bitmap, mAdIconView.getWidth(), mAdIconView.getHeight());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    newBitmap = bitmap;
                                                }

                                                mAdIconView.setImageBitmap(newBitmap);
                                            }

                                            @Override
                                            public void imageFailed(AcbError acbError1) {

                                            }
                                        }, null);
                            }
                        }, null);
                if (!valid) {
                    new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getImageUrl(),
                            ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_IMAGE), new AcbImageLoaderListener() {
                                @Override
                                public void imageLoaded(Bitmap bitmap) {
                                    Bitmap newBitmap;
                                    try {
                                        newBitmap = createCenterCropBitmap(bitmap, mAdIconView.getWidth(), mAdIconView.getHeight());

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        newBitmap = bitmap;
                                    }
                                    mAdIconView.setImageBitmap(newBitmap);
                                }

                                @Override
                                public void imageFailed(AcbError acbError) {

                                }
                            }, null);
                }

                List<View> clickViews = new ArrayList<>();
                if (adContainer.getAdActionView() != null) {
                    clickViews.add(adContainer.getAdActionView());
                }

                if (adContainer.getAdBodyView() != null) {
                    clickViews.add(adContainer.getAdBodyView());
                }

                if (adContainer.getAdTitleView() != null) {
                    clickViews.add(adContainer.getAdTitleView());
                }

                if (adContainer.getAdIconView() != null) {
                    clickViews.add(adContainer.getAdIconView());
                }

                if (adContainer.getAdPrimaryView() != null) {
                    clickViews.add(adContainer.getAdPrimaryView());
                }

                adContainer.setClickViewList(clickViews);

                mAdOrFunctionContainerView.addView(adContainer);
                mAdContainer = adContainer;
                fillNativeAd(ad);
                break;
//            case CHARGE_SCREEN:
//                mAdOrFunctionContainerView.addView(resultView);
//                mAdImageContainer.setVisibility(View.INVISIBLE);
//                mImageIv.setImageResource(R.drawable.charging_screen_guide);
//                mAdIconView.setImageDrawable(AppCompatDrawableManager.get().getDrawable(context, R.drawable.ic_promote_charging_icon));
//                mTitleTv.setText(R.string.result_page_card_battery_protection_title);
//                mDescriptionTv.setText(context.getString(R.string.result_page_card_battery_protection_description));
//                mActionBtn.setText(R.string.result_page_card_battery_protection_btn);
//                mActionBtn.setOnClickListener(this);
//                break;
//            case NOTIFICATION_CLEANER:
//                initNotificationCleanerView();
//                initNotificationCleanerCallbacks();
//                break;
//            case UNREAD_MESSAGE:
//                initUnreadMessageView();
//                break;
//            case WHATS_APP:
//                initWhatsAppView();
//                break;
        }
    }

    void fillNativeAd(AcbNativeAd ad) {
        if (mAdContainer != null) {
            mAdContainer.fillNativeAd(ad);
        }
    }

    protected abstract void onStartTransitionAnimation(View transitionView);

    protected void onFunctionCardViewShown() {

    }

    void startTransitionAnimation() {
        HSLog.d(TAG, "startTransitionAnimation mTransitionView = " + mTransitionView);
        if (null != mTransitionView) {
            onStartTransitionAnimation(mTransitionView);
//            if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER || mType == Type.APP_LOCK
//                    || mType == Type.UNREAD_MESSAGE || mType == Type.WHATS_APP) {
//                if (mResultType != ResultConstants.RESULT_TYPE_JUNK_CLEAN && mResultType != ResultConstants.RESULT_TYPE_CPU_COOLER
//                        && mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER && mResultType != ResultConstants.RESULT_TYPE_BOOST_PLUS
//                        && mResultType != ResultConstants.RESULT_TYPE_BOOST_TOOLBAR && mResultType != ResultConstants.RESULT_TYPE_VIRUS_SCAN && mResultType != ResultConstants.RESULT_TYPE_BATTERY) {
//                    // animation self
//                    startAdOrFunctionResultAnimation(START_DELAY_AD_OR_FUNCTION);
//                }
//            } else {
//                if (mResultType != ResultConstants.RESULT_TYPE_BOOST_PLUS && mResultType != ResultConstants.RESULT_TYPE_BOOST_TOOLBAR
//                        && mResultType != ResultConstants.RESULT_TYPE_JUNK_CLEAN && mResultType != ResultConstants.RESULT_TYPE_CPU_COOLER
//                        && mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER && mResultType != ResultConstants.RESULT_TYPE_VIRUS_SCAN && mResultType != ResultConstants.RESULT_TYPE_BATTERY) {
//                    // animation self
//                    startCardResultAnimation(START_DELAY_CARDS);
//                }
//            }
            if (mType == Type.AD) {
//                if (mResultType != ResultConstants.RESULT_TYPE_BOOST_TOOLBAR) {
//                    startAdOrFunctionResultAnimation(START_DELAY_AD_OR_FUNCTION);
//                }
                startAdOrFunctionResultAnimation(START_DELAY_AD_OR_FUNCTION);
            } else {
                if (mResultType != ResultConstants.RESULT_TYPE_BOOST_TOOLBAR) {
                    startCardResultAnimation(START_DELAY_CARDS);
                }
            }
        }
    }

    public void onTransitionAnimationEnd() {
//        if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER || mType == Type.APP_LOCK
//                || mType == Type.UNREAD_MESSAGE || mType == Type.WHATS_APP) {
        if (mType == Type.AD) {
            startAdOrFunctionResultAnimation(DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY);
//        } else if (mType == Type.CARD_VIEW) {
//            startCardResultAnimation(DURATION_CARD_TRANSLATE_DELAY);
        } else {
            startDefaultViewAnimation();
        }
    }

    public void startDefaultViewAnimation() {
        mResultView.setVisibility(View.VISIBLE);
        adOrFunctionView.setTranslationY(0);

        defaultViewPhone.animate().alpha(1).translationY(0).setDuration(8 * FRAME_40).start();
        defaultViewScreen.animate().alpha(1).translationY(0).setDuration(8 * FRAME_40).start();
        defaultViewDescription.animate().alpha(1).setDuration(7 * FRAME_40).setStartDelay(5 * FRAME_40).start();
        defaultViewBtnOk.animate().alpha(1).scaleX(1).setDuration(8 * FRAME_40).setStartDelay(FRAME_40).start();

        ValueAnimator tickAnimator = ValueAnimator.ofInt(0, 10000);
        tickAnimator.setDuration(7 * FRAME_40);
        tickAnimator.setStartDelay(20 * FRAME_40);
        tickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                int level = (int) animation.getAnimatedValue();
                defaultViewTickDrawable.setLevel(level);
            }
        });
        tickAnimator.start();

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(defaultViewShield,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f));
        animator.setDuration(10 * FRAME_40);
        animator.setStartDelay(21 * FRAME_40);
        animator.start();

        ObjectAnimator star1Animator = ObjectAnimator.ofFloat(defaultViewStar1, View.ALPHA, 0f, 1f, 0.15f, 0.89f);
        star1Animator.setInterpolator(new LinearInterpolator());
        star1Animator.setDuration(26 * FRAME_40);
        star1Animator.setStartDelay(28 * FRAME_40);
        star1Animator.start();

        ObjectAnimator star2Animator = ObjectAnimator.ofFloat(defaultViewStar2, View.ALPHA, 0f, 0.8f, 0.1f, 0.8f);
        star2Animator.setInterpolator(new LinearInterpolator());
        star2Animator.setDuration(28 * FRAME_40);
        star2Animator.setStartDelay(34 * FRAME_40);
        star2Animator.start();

        defaultViewStar3.animate()
                .setInterpolator(new LinearInterpolator())
                .alpha(.6f)
                .setDuration(15 * FRAME_40)
                .setStartDelay(37 * FRAME_40)
                .start();

        defaultViewScreen.postDelayed(new Runnable() {
            @Override public void run() {
                defaultViewFlashView.startFlashAnimation();
            }
        }, 8 * FRAME_40);
    }

    public void startAdOrFunctionResultAnimation(long startDelay) {
        mResultView.postDelayed(new Runnable() {
            @Override public void run() {

                mResultView.setVisibility(View.VISIBLE);
                if (coverShader != null) {
                    coverShader.setVisibility(View.VISIBLE);
                }

                primaryViewContainer.setAlpha(1.0f);
                bottomContainer.setAlpha(1f);
                iconContainer.setAlpha(1f);
                iconContainer.setScaleX(1.0f);
                iconContainer.setScaleY(1.0f);

                ResultController.this.startCardTranslationAnimation(adOrFunctionView, new LauncherAnimationUtils.AnimationListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mActionBtn.startFlash();
                    }
                });
//                }
            }
        }, startDelay);
    }

    public void startCardResultAnimation(long startDelay) {
//        if (mType == Type.CARD_VIEW) {
//            if (null != mResultView) {
//                startBgTranslateAnimation();
//                mResultView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mResultView.setVisibility(View.VISIBLE);
//
//                        startCardTranslationAnimation(mCardRecyclerView, new LauncherAnimationUtils.AnimationListenerAdapter() {
//                            @Override
//                            public void onAnimationEnd(Animation animation) {
//                                super.onAnimationEnd(animation);
//                                onFunctionCardViewShown();
//                            }
//                        });
//                    }
//                }, startDelay);
//            }
//        }
    }

    private void startCardTranslationAnimation(View view, Animation.AnimationListener animatorListenerAdapter) {
        float slideUpTranslation = mScreenHeight - mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_header_height) - Utils.getStatusBarHeight(mActivity) - Utils.pxFromDp(15);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, slideUpTranslation, 0);
        translateAnimation.setDuration(DURATION_CARD_TRANSLATE);
        translateAnimation.setInterpolator(softStopAccDecInterpolator);
        translateAnimation.setAnimationListener(animatorListenerAdapter);
        view.startAnimation(translateAnimation);
    }

    @Thunk
    void startBgTranslateAnimation() {
        if (null == mBgView) {
            return;
        }

        if (mHeaderTagView.getWidth() > 0) {
            startRealBgTranslateAnimation();
        } else {
            mHeaderTagView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mHeaderTagView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mHeaderTagView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    startRealBgTranslateAnimation();
                }
            });
        }
    }

    private void startRealBgTranslateAnimation() {
        float bottom = ViewUtils.getLocationRect(mHeaderTagView).bottom;
        float translationY = -(Utils.getPhoneHeight(getContext()) - bottom);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(translationY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                ResultController.this.setBgViewMargin((int) value);
            }
        });
        valueAnimator.setDuration(DURATION_CARD_TRANSLATE);
        valueAnimator.setInterpolator(softStopAccDecInterpolator);
        valueAnimator.start();
    }

    private void setBgViewMargin(int topMargin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBgView.getLayoutParams();
        params.topMargin = topMargin;
        mBgView.setLayoutParams(params);
    }

    private void setBgViewSize() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBgView.getLayoutParams();
        params.width = Utils.getPhoneWidth(getContext());
        params.height = Utils.getPhoneHeight(getContext());
        mBgView.setLayoutParams(params);

    }

    protected Context getContext() {
        return mActivity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote_charging_button:
                switch (mType) {
//                    case CHARGE_SCREEN:
//                        ChargingScreenSettings.setChargingScreenEnabled(true);
//                        if (HSConfig.optBoolean(false, "Application", "Locker", "AutoOpenWhenSwitchOn") && !LockerSettings.isLockerEverEnabled()) {
//                            LockerSettings.setLockerEnabled(true);
//                        }
//                        ToastUtils.showToast(R.string.result_page_card_battery_protection_toast);
//                        mActivity.finishAndNotify();
//                        break;
//                    case NOTIFICATION_CLEANER:
//                        LauncherAnalytics.logEvent("NotificationCleaner_Guide_Clicked", "type", "ResultPage");
//                        LauncherAnalytics.logEvent("NotificationCleaner_Enterance_Click", "type", NotificationCleanerConstants.RESULT_PAGE);
////                        NotificationCleanerUtils.checkToStartNotificationOrganizerActivity(v.getContext(), NotificationCleanerConstants.RESULT_PAGE);
//                        NotificationCleanerProvider.switchNotificationOrganizer(true);
//                        if (NotificationCleanerUtils.isNotificationAccessGranted(mActivity)) {
//                            NotificationBarUtil.checkToUpdateBlockedNotification();
//                            sendGetActiveNotificationBroadcast();
//                            Intent intentBlocked = new Intent(mActivity, NotificationBlockedActivity.class);
//                            mActivity.startActivity(intentBlocked);
//                            mActivity.finishSelfAndParentActivity();
//                        } else {
//                            LauncherAnalytics.logEvent("NotificationAccess_System_Show", "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//                            startSysSettings();
//                        }
//                        break;
//                    case APP_LOCK:
//                        mActivity.startActivity(new Intent(mActivity, GuideAppProtectedActivity.class)
//                                .putExtra(AppLockConstants.INTENT_EXTRA_APPLOCK_GUIDE_SOURCE_TYPE, AppLockConstants.APPLOCK_GUIDE_SOURCE_TYPE_SIDE_BAR));
//                        mActivity.finishSelfAndParentActivity();
//                        LauncherAnalytics.logEvent("AppLock_RecommendPage_Show", true, "type", "ResultPage");
//                        break;
//                    case UNREAD_MESSAGE:
//                        LauncherAnalytics.logEvent("NotificationAccess_System_Show", "type", ResultConstants.UNREAD_MESSGAE);
//                        startSysSettings();
//                        break;
//                    case WHATS_APP:
//                        LauncherAnalytics.logEvent("NotificationAccess_System_Show", "type", ResultConstants.WHATS_APP);
//                        startSysSettings();
//                        break;
                }
                break;
            default:
                break;
        }
    }

//    private void sendGetActiveNotificationBroadcast() {
//        HSLog.d(NotificationServiceV18.TAG, "NotificationGuideActivity sendGetActiveNotificationBroadcast");
//        Intent broadcastReceiverIntent = new Intent(NotificationServiceV18.ACTION_NOTIFICATION_GET_CURRENT_ACTIVE);
//        broadcastReceiverIntent.setPackage(getPackageName());
//        mActivity.sendBroadcast(broadcastReceiverIntent);
//    }
//
//    private void startSysSettings() {
//        boolean isOpenSettingsSuccess = true;
//        try {
//            Intent intent = new Intent(NotificationCleanerUtils.ACTION_NOTIFICATION_LISTENER_SETTINGS);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mActivity.startActivity(intent);
//        } catch (Exception e) {
//            HSLog.d(TAG, "start system setting error!");
//            isOpenSettingsSuccess = false;
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isOpenSettingsSuccess) {
//            switch (mType) {
//                case NOTIFICATION_CLEANER:
//                    LauncherAnalytics.logEvent("Authority_NotificationAccess_Guide_showed", "type", "notifications cleaner");
//                    LauncherFloatWindowManager.getInstance()
//                            .showPermissionGuide(HSApplication.getContext(), LauncherFloatWindowManager.PermissionGuideType.NOTIFICATION_CLEANER_ACCESS_FULL_SCREEN, false);
//                    break;
//                case UNREAD_MESSAGE:
//                    LauncherAnalytics.logEvent("Authority_NotificationAccess_Guide_showed", "type", "unread message");
//                    LauncherFloatWindowManager.getInstance().showPermissionGuide(HSApplication.getContext(),
//                            LauncherFloatWindowManager.PermissionGuideType.ICON_BADGE, false);
//                    break;
//                case WHATS_APP:
//                    LauncherAnalytics.logEvent("Authority_NotificationAccess_Guide_showed", "type", "whats app");
//                    LauncherFloatWindowManager.getInstance().showPermissionGuide(HSApplication.getContext(),
//                            LauncherFloatWindowManager.PermissionGuideType.ICON_BADGE, false);
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        handler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
//        handler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL);
//        handler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, DELAY_START_TO_PERMISSION_CHECK);
//        handler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL, DURATION_PERMISSION_CHECK_CONTINUED);
//        LauncherAnalytics.logEvent("NotificationCleaner_AccessGuide_Show", true);
//    }

    private void logViewEvent(Type type) {
        if (type == Type.AD) {
            LauncherAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
        }
//        } else if (type == Type.CHARGE_SCREEN) {
//            int shownCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_INTO_BATTERY_PROTECTION_COUNT);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_INTO_BATTERY_PROTECTION_SHOWN_TIME, System.currentTimeMillis());
//
//            LauncherAnalytics.logEvent("ResultPage_Cards_Show", true, "type", ResultConstants.CHARGING_SCREEN_FULL);
//            LauncherAnalytics.logEvent("ResultPage_Battery_Show", true, "type", getLogEventType(shownCount));
//        } else if (type == Type.NOTIFICATION_CLEANER) {
//            int shownCount = PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_INTO_NOTIFICATION_CLEANER_SHOWN_TIME, System.currentTimeMillis());
//
//            LauncherAnalytics.logEvent("ResultPage_Cards_Show", true, "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//            LauncherAnalytics.logEvent("ResultPage_Notification_Show", true, "type", getLogEventType(shownCount));
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Show", true, "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//        } else if (type == Type.APP_LOCK) {
//            int shownCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_INTO_APP_LOCK_COUNT);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_INTO_APP_LOCK_SHOWN_TIME, System.currentTimeMillis());
//
//            LauncherAnalytics.logEvent("ResultPage_Cards_Show", true, "type", ResultConstants.APPLOCK);
//            LauncherAnalytics.logEvent("ResultPage_App_Show", true, "type", getLogEventType(shownCount));
//        } else if (type == Type.UNREAD_MESSAGE) {
//            int shownCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_INTO_UNREAD_MESSAGE_COUNT);
//
//            LauncherAnalytics.logEvent("ResultPage_Cards_Show", true, "type", ResultConstants.UNREAD_MESSGAE);
//            LauncherAnalytics.logEvent("ResultPage_Notification_Show", true, "type", getLogEventType(shownCount));
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Show", true, "type", ResultConstants.UNREAD_MESSGAE);
//        } else if (type == Type.WHATS_APP) {
//            int shownCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(ResultConstants.PREF_KEY_INTO_WHATS_APP_COUNT);
//
//            LauncherAnalytics.logEvent("ResultPage_Cards_Show", true, "type", ResultConstants.WHATS_APP);
//            LauncherAnalytics.logEvent("ResultPage_Notification_Show", true, "type", getLogEventType(shownCount));
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Show", true, "type", ResultConstants.WHATS_APP);
//        }

        LauncherAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
    }

    protected void logClickEvent(Type type) {
//        if (type == Type.AD) {
//            // No log here, logged in onAdClick()
//        } else if (type == Type.CHARGE_SCREEN) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", true, "Type", ResultConstants.CHARGING_SCREEN_FULL);
//            LauncherAnalytics.logEvent("ResultPage_Battery_Clicked", true, "Type", getLogEventType(
//                    PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(ResultConstants.PREF_KEY_INTO_BATTERY_PROTECTION_COUNT, 1)));
//        } else if (type == Type.NOTIFICATION_CLEANER) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", true, "Type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//            LauncherAnalytics.logEvent("ResultPage_Notification_Clicked", true, "Type", getLogEventType(
//                    PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS).getInt(ResultConstants.PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT, 1)));
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Clicked", true, "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//        } else if (type == Type.APP_LOCK) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", true, "Type", ResultConstants.APPLOCK);
//            LauncherAnalytics.logEvent("ResultPage_App_Clicked", true, "Type", getLogEventType(
//                    PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_INTO_APP_LOCK_COUNT, 1)));
//        } else if (type == Type.UNREAD_MESSAGE) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", true, "Type", ResultConstants.UNREAD_MESSGAE);
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Clicked", true, "type", ResultConstants.UNREAD_MESSGAE);
//        } else if (type == Type.WHATS_APP) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", true, "Type", ResultConstants.WHATS_APP);
//            LauncherAnalytics.logEvent("ResultPage_NotificationAccess_Clicked", true, "type", ResultConstants.WHATS_APP);
//        }
    }

    private String getLogEventType(int order) {
        switch (order) {
            case 1:
                return "First";
            case 2:
                return "Second";
            case 3:
                return "Third";
            default:
                return "other";
        }
    }

    private Bitmap createCenterCropBitmap(@NonNull Bitmap bitmap, float width, float height) throws Exception {
        if (width <= 0 || height <= 0) {
            return bitmap;
        }

        final float bWidth = bitmap.getWidth();
        final float bHeight = bitmap.getHeight();

        if (width / height >= bWidth / bHeight) {
            return Bitmap.createBitmap(bitmap, 0, 0, (int) bWidth, (int) (bWidth * height / width));
        }

        final float offset = (bWidth - bHeight * width / height) / 2f;
        return Bitmap.createBitmap(bitmap, (int) offset, 0, (int) (bWidth - 2f * offset), (int) bHeight);
    }

    protected void popupInterstitialAdIfNeeded() {
        if (mInterstitialAd != null) {
            popupInterstitialAd();
        } else {
            if (startPromoteGuide(mActivity)) {
//                HSGlobalNotificationCenter.addObserver(PromoteUtils.NOTIFICATION_RESULT_PAGE_PROMOTION_FINISH, new INotificationObserver() {
//                    @Override public void onReceive(String s, HSBundle hsBundle) {
//                        HSGlobalNotificationCenter.removeObserver(this);
//                        if (!mActivity.isAttached()) {
//                            return;
//                        }
//                        new Handler().postDelayed(new Runnable() {
//                            @Override public void run() {
//                                onInterruptActionClosed();
//                            }
//                        }, 250);
//                    }
//                });
            } else {
                onInterruptActionClosed();
            }
        }
    }

    private static boolean startPromoteGuide(Context activity) {
//        if (PromoteUtils.isResultPageEnable()) {
//            PromoteUtils.showPromoteGuideWithoutIcon(activity, "ResultPage", PromoteGuideActivity.PromoteType.RESULT_PAGE);
//            return true;
//        }
        return false;
    }

    private void popupInterstitialAd() {
        mInterstitialAd.setCustomTitle(HSApplication.getContext().getString(R.string.boost_plus_optimal));
        mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
            @Override
            public void onAdDisplayed() {

            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.release();
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        onInterruptActionClosed();
                    }
                }, 250);
            }
        });
        mInterstitialAd.show();
    }

    protected abstract void onInterruptActionClosed();
}
