package colorphone.acb.com.libscreencard.gif;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
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
        return "";
    }

    private String getAdPlacements() {
        // TODO
        return "";
    }
    private void initView() {
        if (sBlurWallpaper != null) {
            findViewById(R.id.root_view).setBackgroundDrawable(sBlurWallpaper);
        }
        mVp = findViewById(R.id.vp);
        mVp.setPageMargin(Dimensions.pxFromDp(30));
        mAdapter = new GifAdapter();
        mVp.setAdapter(mAdapter);
        mVp.setCurrentItem(mInitPosition);
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
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
                Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).putBoolean(PREF_KEY_HAVE_SLID, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
        boolean haveSlid = Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).getBoolean(PREF_KEY_HAVE_SLID, false);
        if (!haveSlid) {
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
        mArrow = findViewById(R.id.guide_arrow);

        int distance1 = Dimensions.pxFromDp(130);
        ValueAnimator enter1 = ValueAnimator.ofFloat(0f, 1f);
        enter1.addUpdateListener((animation -> {
            float animatedFraction = animation.getAnimatedFraction();
            mArrow.setTranslationX(-animatedFraction * distance1);
            mArrow.setAlpha(animatedFraction);
        }));
        enter1.setDuration(400);

        int distance2 = Dimensions.pxFromDp(90);
        ValueAnimator quit1 = ValueAnimator.ofFloat(0f, 1f);
        quit1.addUpdateListener(animation -> {
            mArrow.setTranslationX(-(distance1 - animation.getAnimatedFraction() * (distance1 - distance2)));
        });
        quit1.setDuration(400);

        ValueAnimator enter2 = ValueAnimator.ofFloat(0f, 1f);
        enter2.addUpdateListener(animation -> {
            mArrow.setTranslationX(-(distance2 + animation.getAnimatedFraction() * (distance1 - distance2)));
        });
        enter2.setDuration(400);

        ValueAnimator quit2 = ValueAnimator.ofFloat(0f, 1f);
        quit2.addUpdateListener(animation -> {
            mArrow.setTranslationX(-(distance1 - animation.getAnimatedFraction() * (distance1 - distance2)));
        });
        quit2.setDuration(400);

        ValueAnimator enter3 = ValueAnimator.ofFloat(0f, 1f);
        enter3.addUpdateListener(animation -> {
            mArrow.setTranslationX(-(distance2 + animation.getAnimatedFraction() * (distance1 - distance2)));
        });
        enter3.setDuration(400);

        ValueAnimator quit3 = ValueAnimator.ofFloat(0f, 1f);
        quit3.addUpdateListener(animation -> {
            float animatedFraction = animation.getAnimatedFraction();
            mArrow.setTranslationX(-(distance1 - animatedFraction * (distance1 - distance2)));
            mArrow.setAlpha(1 - animatedFraction);
        });
        quit3.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mArrow.setVisibility(View.VISIBLE);
            }
        });
        animatorSet.playSequentially(enter1, quit1, enter2, quit2, enter3, quit3);
        Threads.postOnMainThreadDelayed(animatorSet::start, 1000);
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
}
