package colorphone.acb.com.libscreencard.gif;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.expressad.AcbExpressAdManager;
import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.util.Map;

import colorphone.acb.com.libscreencard.LockerCustomConfig;
import colorphone.acb.com.libscreencard.R;

public class GifCenterActivity extends HSAppCompatActivity implements AcbInterstitialAd.IAcbInterstitialAdListener {

    private static final String TAG = GifCenterActivity.class.getSimpleName();
    private static final String PREF_KEY_HAVE_SLID = "have_slid";

    public static final String INTENT_EXTRA_DATA_KEY_INIT_POSITION = "init_position";

    public static Drawable sBlurWallpaper;

    private int mInterstitialAdInterval = HSConfig.optInteger(4, "Application", "SecurityProtection", "GIFAdInterval");
    private int mScrolledCount;
    private int mInitPosition;

    private AcbInterstitialAd mAcbInterstitialAd;

    private ViewPager mVp;
    private PagerAdapter mAdapter;
    private Map<String, String> sGifMap = GifCacheUtils.getGif();
    private ViewGroup mExpressAdContainer;
    private View mArrow;
    private Guide mGuide;


    private AdLogger mExpressLogger = new AdLogger(getAdPlacements());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_center);
        AcbExpressAdManager.getInstance().activePlacementInProcess(getAdPlacements());
        mInitPosition = getIntent().getIntExtra(INTENT_EXTRA_DATA_KEY_INIT_POSITION, 0);
        LocalInterstitialAdPool.getInstance().preload(getInterstitialAdPlacements());
        mExpressLogger.adSessionStart();
        initView();
    }

    private String getInterstitialAdPlacements() {
        // TODO
        return "GifInterstitial";
    }

    private String getAdPlacements() {
        // TODO
        return "GifExpress";
    }
    private void initView() {
        if (sBlurWallpaper != null) {
            findViewById(R.id.root_view).setBackgroundDrawable(sBlurWallpaper);
        } else {
            findViewById(R.id.root_view).setBackgroundColor(Color.parseColor("#ff4285f4"));
        }
        mGuide = new Guide(findViewById(R.id.guide_arrow_left), findViewById(R.id.guide_arrow_right));
        mVp = findViewById(R.id.vp);
        mVp.setPageMargin(Dimensions.pxFromDp(30));
        mAdapter = new GifAdapter();
        mVp.setAdapter(mAdapter);
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mGuide.setProgress(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                mGuide.showLeft(position > 0);

                mScrolledCount++;
                if (mScrolledCount % mInterstitialAdInterval == 0) {
                    mAcbInterstitialAd = LocalInterstitialAdPool.getInstance().fetch(getInterstitialAdPlacements());
                    LocalInterstitialAdPool.getInstance().preload(getInterstitialAdPlacements());
                    if (mAcbInterstitialAd != null) {
                        mAcbInterstitialAd.setInterstitialAdListener(GifCenterActivity.this);
                        mAcbInterstitialAd.show();
                        LockerCustomConfig.logAdViewEvent(getInterstitialAdPlacements(), true);
                        AutoPilotUtils.logGIFInterstitialAdShow();
                    } else {
                        LockerCustomConfig.logAdViewEvent(getInterstitialAdPlacements(), false);
                    }
                }
                int currentViewedGifKey = GifCacheUtils.getCurrentViewedGifKey();
                if (position + 1 >= currentViewedGifKey) {
                    GifCacheUtils.setCurrentViewedGifKey(position + 2);
                    GifCacheUtils.markCachedGifViewedState(false);
                }
                Preferences.get(CardConfig.CARD_MODULE_PREFS).putBoolean(PREF_KEY_HAVE_SLID, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mGuide.scheduleNextHint();
                } else {
                    mGuide.cancelSchedule();
                }

            }
        });
        mVp.setCurrentItem(mInitPosition);

        mExpressAdContainer = findViewById(R.id.ad_container);
        AcbExpressAdView expressAdView = new AcbExpressAdView(this, getAdPlacements());
        expressAdView.prepareAd(new AcbExpressAdView.PrepareAdListener() {

            @Override
            public void onAdReady(AcbExpressAdView acbExpressAdView) {

            }

            @Override
            public void onPrepareAdFailed(AcbExpressAdView acbExpressAdView) {

            }
        });
        expressAdView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_All);
        expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {

            @Override
            public void onAdShown(AcbExpressAdView acbExpressAdView) {
                mExpressLogger.adShow();
                AutoPilotUtils.logGIFExpressAdShow();
            }

            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {

            }
        });
        mExpressAdContainer.addView(expressAdView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean haveSlid = Preferences.get(CardConfig.CARD_MODULE_PREFS).getBoolean(PREF_KEY_HAVE_SLID, false);
        if (!haveSlid || HSLog.isDebugging()) {
            Threads.postOnMainThreadDelayed(this::showGuideAnimation, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sBlurWallpaper = null;
        mExpressLogger.adSessionEnd();
        AcbExpressAdManager.getInstance().deactivePlacementInProcess(getAdPlacements());
    }

    @Override
    public void onAdDisplayed() {

    }

    @Override
    public void onAdClicked() {

    }

    @Override
    public void onAdClosed() {
        if (mAcbInterstitialAd != null) {
            mAcbInterstitialAd.release();
            mAcbInterstitialAd.setInterstitialAdListener(null);
            mAcbInterstitialAd = null;
        }
    }

    private void showGuideAnimation() {
        mGuide.firstAnim();
    }

    private class GifAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return sGifMap.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            GifCenterItemView gifCenterItemView = new GifCenterItemView(GifCenterActivity.this, sGifMap.get(String.valueOf(position + 1)));
            container.addView(gifCenterItemView);
            return gifCenterItemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private class Guide {
        private View mArrowLeft;
        private View mArrowRight;
        private float mDistance = Dimensions.pxFromDp(16);
        private ValueAnimator valueAnimator;
        private ValueAnimator startAnim;
        private TimeInterpolator mTimeInterpolator = PathInterpolatorCompat.create
                (.58f, .01f, .44f, .99f);
        private TimeInterpolator mAdInterpolator = new AccelerateDecelerateInterpolator();
        private TimeInterpolator mLinInterpolator = new LinearInterpolator();
        private TimeInterpolator mDecInterpolator = new DecelerateInterpolator();
        private boolean animRight = false;
        private boolean animLeft = false;
        private final ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float trans = (float) animation.getAnimatedValue();
                if (animRight) {
                    mArrowRight.setTranslationX(trans);
                }
                if (animLeft) {
                    mArrowLeft.setTranslationX(-trans);
                }
            }
        };
        private final ValueAnimator.AnimatorListener mListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reset();
            }
        };

        private Runnable autoHintTask = new Runnable() {
            @Override
            public void run() {
                valueAnimator.setRepeatCount(1);
                doAnimation();
                scheduleNextHint();
            }
        };

        public Guide(View arrowLeft, View arrowRight) {
            mArrowLeft = arrowLeft;
            mArrowRight = arrowRight;
            initAnimators();
        }

        private void initAnimators() {
            valueAnimator = ValueAnimator.ofFloat(0, mDistance).setDuration(600);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setRepeatCount(1);
            valueAnimator.setInterpolator(new TimeInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    return makeFraction(input);
                }
            });
            valueAnimator.addUpdateListener(mUpdateListener);
            valueAnimator.addListener(mListener);
        }

        public void firstAnim() {
            animLeft = false;
            animRight = true;
            startAnim = ValueAnimator.ofFloat(0, mDistance).setDuration(600);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            startAnim.setRepeatCount(3);
            startAnim.addUpdateListener(mUpdateListener);
            startAnim.addListener(mListener);
            startAnim.setInterpolator(new TimeInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    float fractionValue;
                    if (input <= 0.5f) {
                        fractionValue = mAdInterpolator.getInterpolation(input * 2f);
                    } else {
                        fractionValue = mLinInterpolator.getInterpolation((1f - input) * 2f);
                    }
                    return fractionValue;
                }
            });

            startAnim.start();
        }

        public void setProgress(float progress) {
            animLeft = false;
            animRight = false;
            float fractionValue = makeFraction(progress);

            float transX = fractionValue * mDistance * 1.3f;
            mArrowRight.setTranslationX(transX);
            mArrowLeft.setTranslationX(-transX);
        }

        private float makeFraction(float progress) {
            float fractionValue;
            if (progress <= 0.5f) {
                fractionValue = mAdInterpolator.getInterpolation(progress * 2f);
            } else {
                fractionValue = mDecInterpolator.getInterpolation((1f - progress) * 2f);
            }
            return fractionValue;
        }

        public void doAnimation() {
            doAnimation(true, false);
        }

        private void doAnimation(boolean right, boolean left) {
            animLeft = left;
            animRight = right;
            if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            valueAnimator.start();
        }

        private void reset() {
            animRight = false;
            animLeft = false;
            mArrowLeft.setTranslationX(0);
            mArrowRight.setTranslationX(0);
        }

        public void showLeft(boolean show) {
            mArrowLeft.animate().alpha(show ? 1f : 0f).setDuration(200).start();
        }

        public void cancelSchedule() {
            mArrowRight.removeCallbacks(autoHintTask);
        }

        public void scheduleNextHint() {
            cancelSchedule();
            mArrowRight.postDelayed(autoHintTask, 10000);
        }
    }
}
