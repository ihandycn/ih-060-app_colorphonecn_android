package com.honeycomb.colorphone.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.feedback.FeedbackActivity;
import com.honeycomb.colorphone.feedback.HuaweiRateGuideDialog;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.TypefacedTextView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@SuppressLint("ViewConstructor")
public class FiveStarRateTip extends DefaultButtonDialog2 implements View.OnClickListener {
    private static final String TAG = FiveStarRateTip.class.getSimpleName();
    private static final boolean DEBUG_FIVE_STAR = false && BuildConfig.DEBUG;

    private static final String PREF_KEY_FIVE_STAR_SHOWED_THEME = "PREF_KEY_FIVE_STAR_SHOWED_THEME";
    private static final String PREF_KEY_FIVE_STAR_SHOWED_COUNT = "PREF_KEY_FIVE_STAR_SHOWE_COUNT";
    private static final String PREF_KEY_FIVE_STAR_SHOWED_END_CALL = "PREF_KEY_FIVE_STAR_SHOWED_END_CALL";
    private static final String PREF_KEY_HAD_FIVE_STAR_RATE = "pref_key_had_five_star_rate";

    public static final String FIVE_START_TIP_DISMISS = "five_start_tip_dismiss";

    private static final long CHANGE_DURATION = 500;
    private static final long ONE_STEP_DURATION = 200;
    private static final long ANIM_DELAY = 200;
    private static final long ALL_STEPS_DURATION = 5 * ONE_STEP_DURATION;
    private static int sCurrentSessionId = -1;

    public enum From {
        SET_THEME(0),
        //@Deprecated
        END_CALL(1);

        private int code = 0;

        From(int code) {
            this.code = code;
        }

        public int value() {
            return this.code;
        }

        public String toString() {
            switch (this.code) {
                case 0:
                    int showCount = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_FIVE_STAR_SHOWED_COUNT, 0);
                    if (showCount <= 1) {
                        return "ApplyFinished";
                    } else {
                        return "SecondApplyFinished";
                    }
                case 1:
                    return "CallFinished";
                default:
                    return "";
            }
        }

        public static From valueOf(int code) {
            switch (code) {
                case 0:
                    return SET_THEME;
                case 1:
                    return END_CALL;
                default:
                    return null;
            }
        }
    }

    private From mFrom;

    private static final int MAX_ANIM_COUNT = 2;
    private static final int INVALID_POSITION = -1;
    private static final int STAR_COUNT = 5;
    private static final int MAX_POSITION = STAR_COUNT - 1;
    private static final int MIN_POSITION = 0;
    private static final float PERCENT_ZERO = 0.0f;
    private static final float PERCENT_ONE_STAR = 0.342f;
    private static final float PERCENT_TWO_STAR = 0.684f;
    private static final float PERCENT_THREE_STAR = 0.763f;
    private static final float PERCENT_FOUR_STAR = 0.842f;
    private static final float PERCENT_FIVE_STAR = 1.0f;
    private static final float[] mTimePoint = {
            PERCENT_ZERO, PERCENT_ONE_STAR, PERCENT_TWO_STAR, PERCENT_THREE_STAR, PERCENT_FOUR_STAR, PERCENT_FIVE_STAR
    };
    private static final int[] mDescTexts = {
            R.string.five_star_one_text, R.string.five_star_two_text, R.string.five_star_three_text,
            R.string.five_star_four_text, R.string.five_star_five_text
    };
    private static final int[] mDialogDescTexts = {
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.loved_it_desc
    };

    private boolean mAnimViewShowed = false;
    private DisplayMetrics mMetrics;
    private ImageView[] mStarViews;
    private ImageView[] mGuideStarViews;
    private LinearLayout mGuideLayout;
    private FiveStarLayout mRateLayout;
    private LottieAnimationView mAnimationView;
    private Cancellable mAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
            "lottie/five_star_rating.json", new OnCompositionLoadedListener() {
                @Override public void onCompositionLoaded(@Nullable LottieComposition lottieComposition) {
                    if (!mAnimViewShowed) {
                        mAnimViewShowed = true;
                        mAnimationView.setComposition(lottieComposition);
                        FiveStarRateTip.this.guideAnim(true);
                        FiveStarRateTip.this.show();
                    }
                }
            });
    private ImageView mStillView;
    private TypefacedTextView mStarDescView;
    private ImageView mHandImageView;
    private int mCurrentPosition = INVALID_POSITION;
    private TextView mDescTv;
    private AnimatorSet mAnimatorSet;
    private int mAnimCount;

    private FiveStarRateTip(Context context, From from) {
        super(context);
        mMetrics = context.getResources().getDisplayMetrics();
        mFrom = from;
        mStarViews = new ImageView[STAR_COUNT];
        mGuideStarViews = new ImageView[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            mStarViews[i] = new ImageView(context);
            mStarViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mStarViews[i].setImageResource(R.drawable.star_dark);
            mStarViews[i].setTag(i);
            mStarViews[i].setOnClickListener(this);

            mGuideStarViews[i] = new ImageView(context);
            mGuideStarViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mGuideStarViews[i].setImageResource(R.drawable.star_dark);
        }

        Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_FIVE_STAR_SHOWED_COUNT);
        sCurrentSessionId = SessionMgr.getInstance().getCurrentSessionId();

        Analytics.logEvent("RateAlert_Showed", "type", mFrom.toString());

    }

    @Override
    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.dialog_five_star, root, false);
        mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);
        mDescTv.setText(getResources().getString(R.string.five_star_hint_text));

        // Add LottieAnimationView into TopImage container.
        View faceView = inflater.inflate(R.layout.dialog_five_start_face, root, false);
        mStillView = ViewUtils.findViewById(faceView, R.id.still_view);
        mStillView.setVisibility(GONE);
        mAnimationView = ViewUtils.findViewById(faceView, R.id.animation_view);
        ViewGroup imageContainer = ViewUtils.findViewById(mRootView, R.id.dialog_image_container);
        imageContainer.addView(faceView);
        mStarDescView = ViewUtils.findViewById(v, R.id.star_desc);

        mRateLayout = ViewUtils.findViewById(v, R.id.rate_area);
        mRateLayout.setOnMoveListener(new FiveStarLayout.OnMoveListener() {
            @Override
            public void onMove(boolean isToRight, int position, float progress) {
                onMoveChangingView(isToRight, position, progress);
            }

            @Override
            public void onUp(int position) {
                onUpChangingView(position);
            }
        });
        mGuideLayout = ViewUtils.findViewById(v, R.id.guide_rate_area);

        mRateLayout.setGravity(Gravity.CENTER);
        mGuideLayout.setGravity(Gravity.CENTER);
        for (int i = 0; i < STAR_COUNT; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Dimensions.pxFromDp(26.4f), 1);
            mRateLayout.addView(mStarViews[i], params);
            mGuideLayout.addView(mGuideStarViews[i], params);
        }

        mHandImageView = ViewUtils.findViewById(v, R.id.hand_img);
        loadAnimation();
        mAnimCount = 0;

        return v;
    }

    private void loadAnimation() {
        cancelAnimationLoadTask();
        try {
            mAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/five_star_rating.json", new OnCompositionLoadedListener() {
                        @Override
                        public void onCompositionLoaded(@Nullable LottieComposition lottieComposition) {
                            if (!mAnimViewShowed) {
                                mAnimViewShowed = true;
                                mAnimationView.setComposition(lottieComposition);
                                FiveStarRateTip.this.guideAnim(true);
                                FiveStarRateTip.this.show();
                            }
                        }
                    });
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getPositiveButtonStringId() {
        return R.string.five_star_positive_text;
    }

    @Override protected int getNegativeButtonStringId() {
        return R.string.cancel;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        if (mCurrentPosition >= 0) {
            if (mCurrentPosition == MAX_POSITION) {
                //HSMarketUtils.browseAPP();
                launchAppDetail(HSApplication.getContext().getPackageName(), "com.huawei.appmarket");
                Analytics.logEvent("RateAlert_Fivestar_Submit", "type", mFrom.toString());
            } else {
//                Utils.sentEmail(getContext(), new String[]{Constants.getFeedBackAddress()}, null, null);
                Analytics.logEvent("RateAlert_Lessthanfive_Submit", "type", mFrom.toString());
                Navigations.startActivitySafely(getContext(), FeedbackActivity.class);
            }
            Preferences.get(Constants.DESKTOP_PREFS).putBoolean(PREF_KEY_HAD_FIVE_STAR_RATE, true);
            markAlertLifeOver();
            dismiss();
        } else {
            guideAnim(false);
        }
    }

    private void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) {
                return;
            }

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            HSApplication.getContext().startActivity(intent);

            Threads.postOnMainThreadDelayed(() -> {
                HuaweiRateGuideDialog.show(getContext());
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onClickNegativeButton(View v) {
    }

    @Override
    protected void onCanceled() {
    }

    @Override
    protected Drawable getTopImageDrawable() {
        return ContextCompat.getDrawable(mActivity, R.drawable.dialog_five_star_top);
    }

    @Override
    protected boolean fitImageWidth() {
        return true;
    }

    private void onMoveChangingView(boolean isToRight, int position, float progress) {
        if ((isToRight && mCurrentPosition < MAX_POSITION) || (!isToRight && mCurrentPosition > MIN_POSITION)) {
            resetStarView(position);
            mCurrentPosition = position;
            mAnimationView.setProgress(progress);
        }
    }

    private void onUpChangingView(int position) {
        if (position == mCurrentPosition) {
            float progress = mTimePoint[getTimePosition(position)];
            mAnimationView.setProgress(progress);
        }
    }

    private void resetStarView(int position) {
        if (position > mCurrentPosition) {
            for (int k = mCurrentPosition == -1 ? 0 : mCurrentPosition; k <= position; k++) {
                mStarViews[k].setImageResource(R.drawable.star_light);
            }
        } else {
            for (int i = position + 1; i <= mCurrentPosition; i++) {
                mStarViews[i].setImageResource(R.drawable.star_dark);
            }
        }
        mStarDescView.setText(getResources().getString(mDescTexts[position]));
        mDescTv.setText(getResources().getString(mDialogDescTexts[position]));
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if (tag == mCurrentPosition) {
            return;
        }
        resetStarView(tag);
        int last = mCurrentPosition < 0 ? 3 : (mCurrentPosition < 2 ? 0 : mCurrentPosition);
        getAnimator(mTimePoint[getTimePosition(last)], mTimePoint[getTimePosition(tag)]).setDuration(CHANGE_DURATION).start();
        mCurrentPosition = tag;
    }

    private int getTimePosition(int position) {
        return position + 1;
    }

    private void initAnim() {
        ObjectAnimator[] objectAnimatorArray = new ObjectAnimator[mGuideStarViews.length];
        for (int i = 0; i < mGuideStarViews.length; i++) {
            objectAnimatorArray[i] = ObjectAnimator.ofFloat(mGuideStarViews[i], "alpha", 0f, 1f).setDuration(ONE_STEP_DURATION);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimCount++;
                mGuideLayout.setVisibility(VISIBLE);
                for (int i = 0; i < mGuideStarViews.length; i++) {
                    mGuideStarViews[i].setImageResource(R.drawable.star_light);
                    mGuideStarViews[i].setAlpha(0f);
                }
                mHandImageView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mGuideLayout.setVisibility(GONE);
                mHandImageView.setVisibility(GONE);
                if (mAnimCount < MAX_ANIM_COUNT) {
                    guideAnim(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mGuideLayout.setVisibility(GONE);
                mHandImageView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        ValueAnimator animator = getAnimator(PERCENT_ONE_STAR, PERCENT_FIVE_STAR).setDuration(ALL_STEPS_DURATION);
        int dis = Dimensions.pxFromDp(188);
        ObjectAnimator handAnimator = ObjectAnimator.ofFloat(mHandImageView, "translationX", dis).setDuration(ALL_STEPS_DURATION);
        mAnimatorSet.playSequentially(
                objectAnimatorArray[0],
                objectAnimatorArray[1],
                objectAnimatorArray[2],
                objectAnimatorArray[3],
                objectAnimatorArray[4]
        );
        mAnimatorSet.playTogether(animator, handAnimator);
    }

    private void guideAnim(boolean isDelay) {
        if (mAnimatorSet == null) {
            initAnim();
        }
        if (isDelay) {
            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override public void run() {
                    if (mAnimatorSet != null) {
                        mAnimatorSet.start();
                    }
                }
            }, ANIM_DELAY);
        } else {
            mAnimatorSet.start();
        }
    }

    private ValueAnimator getAnimator(final float from, float to) {
        if (mAnimationView == null || mAnimationView.getVisibility() != VISIBLE) {
            return null;
        }
        final float total = to - from;
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float progress = from + total * animation.getAnimatedFraction();
                mAnimationView.setProgress(progress);
            }
        });
        return animator;
    }

    /**
     * We do not need show this alert again.
     */
    private void markAlertLifeOver() {
        switch (mFrom) {
            case SET_THEME:
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(FiveStarRateTip.PREF_KEY_FIVE_STAR_SHOWED_THEME, true);
                break;
            case END_CALL:
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(FiveStarRateTip.PREF_KEY_FIVE_STAR_SHOWED_END_CALL, true);
                break;
        }
    }

    public static void show(Context context, From from) {
        new FiveStarRateTip(context, from).show();
    }

    @Override
    protected void onDismissComplete() {
        super.onDismissComplete();
        cancelAnimationLoadTask();
        HSGlobalNotificationCenter.sendNotification(FIVE_START_TIP_DISMISS);
    }

    private void cancelAnimationLoadTask() {
        if (mAnimationLoadTask != null) {
            mAnimationLoadTask.cancel();
            mAnimationLoadTask = null;
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
    }

    public static boolean canShowWhenApplyTheme() {
        return isNewUser() && !isHadFiveStarRate() && isFiveStarRateShownByFrom(From.SET_THEME);
    }

    public static boolean canShowWhenEndCall() {
        return isNewUser() && !isHadFiveStarRate() && isFiveStarRateShownByFrom(From.END_CALL);
    }

    private static boolean isNewUser() {
        Locale current = Dimensions.getLocale(HSApplication.getContext());
        String myCountry = current.getCountry().toLowerCase();
        List<String> filter = (List<String>) HSConfig.getList("Application", "RateAlert", "UnsupportedCountry");
        if (filter != null && filter.size() > 0) {
            for (String country : filter) {
                if (TextUtils.equals(country.toLowerCase(), myCountry)) {
                    HSLog.i("FiveStarRateTip", "not support " + country);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isHadFiveStarRate() {
        return !DEBUG_FIVE_STAR && Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_HAD_FIVE_STAR_RATE, false);
    }

    private static boolean isFiveStarRateShownByFrom(From from) {
        switch (from) {
            case SET_THEME:
                return DEBUG_FIVE_STAR ||
                        SessionMgr.getInstance().getCurrentSessionId() != sCurrentSessionId
                        && !Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_FIVE_STAR_SHOWED_THEME, false)
                        && isApplyCountValid();
            case END_CALL:
                return DEBUG_FIVE_STAR ||
                        (HSConfig.optBoolean(true, "Application", "RateAlert", "CallFinished", "Enable")
                        && !Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_FIVE_STAR_SHOWED_END_CALL, false));
        }
        return true;
    }

    private static boolean isApplyCountValid() {
        int hasShowCount = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_FIVE_STAR_SHOWED_COUNT, 0);
        if (hasShowCount == 0) {
            return HSConfig.optBoolean(true, "Application", "RateAlert", "ApplyFinished", "Enable");
        } else if (hasShowCount == 1) {
            return HSConfig.optBoolean(true, "Application", "RateAlert", "SecondApplyFinished", "Enable");
        }
        return false;

    }
}
