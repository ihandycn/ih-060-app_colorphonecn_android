package com.acb.libwallpaper.live.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.acb.libwallpaper.live.util.ViewUtils;
 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.util.StringUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;

public class PermissionGuideView extends RelativeLayout {

    private static final long FRAME = 100;
    private static final long FRAME_MIN = 50;

    private static final long DURATION_APPEAR_OPEN = 3 * FRAME;
    private static final long DURATION_FINGER_TRANSLATE_TO_TOP = 7 * FRAME;
    private static final long DURATION_FINGER_TRANSLATE_TO_PRESS = 4 * FRAME;
    private static final long DURATION_FINGER_PRESSED = 5 * FRAME;
    private static final long DURATION_HIGH_LIGHT_RECTANGLE = 3 * FRAME;
    private static final long DURATION_FINGER_TRANSLATE_TOP_TO_OPEN = 4 * FRAME;
    private static final long DURATION_FINGER_OPEN_SWITCH = 5 * FRAME_MIN;
    private static final long DURATION_FINGER_DISAPPEAR = 3 * FRAME;
    private static final long DURATION_SCROLL_VIEW_DISAPPEAR = 3 * FRAME;
    private static final long DURATION_LAST_VIEW_APPEAR = 3 * FRAME;
    private static final long DURATION_LAST_VIEW_DISAPPEAR = 3 * FRAME;
    private static final long DURATION_DIALOG_APPEAR = 3 * FRAME;
    private static final long DURATION_RIPPLE_APPEAR = 5 * FRAME;

    private static final long DELAY_FINGER_DISAPPEAR = FRAME;
    private static final long DELAY_LAST_VIEW_DISAPPEAR = 2 * FRAME;

    private AnimatorSet mAnimations;
    private boolean mAnimationsCancelled;
    private boolean mIsShowConfirmDialog = false;

    private float mDensity;
    private View mFingerView;
    private ImageView mFingerTagView;
    private PermissionGuideLastView mLastView;
    private RippleView mRippleView;
    private ScrollView mScrollView;
    private ToggleView mLastViewToggleView;
    private ToggleView mFrontViewToggleView;
    private TextView mTitleTv;
    private TextView mContentTv;
    private Interpolator mDefaultInterpolator;
    private AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    private View mDialogView;
    private View mDialogBg;
    private View mCircleView;
    private Path mPath;
    private RectF mRectf;

    public PermissionGuideView(Context context) {
        super(context);
        init(context);
    }

    public PermissionGuideView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public PermissionGuideView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.permission_guide_animation_content, this, true);
        this.mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        this.mScrollView.requestDisallowInterceptTouchEvent(true);
        this.mScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
        });
        this.mRippleView = (RippleView) this.findViewById(R.id.ripple_view);
        this.mFingerTagView = (ImageView) findViewById(R.id.finger_tag_iv);
        this.mLastView = (PermissionGuideLastView) findViewById(R.id.permission_guide_last_view);
        this.mFrontViewToggleView = findViewById(R.id.permission_guide_toggle_view);
        this.mLastViewToggleView = (ToggleView) findViewById(R.id.last_view_toggle_view);
        mTitleTv = ViewUtils.findViewById(this, R.id.permission_guide_title_tv);
        mContentTv = ViewUtils.findViewById(this, R.id.permission_guide_content_tv);
        this.mDensity = getResources().getDisplayMetrics().density;
        mDefaultInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f);
        this.mDialogView = findViewById(R.id.permission_guide_confirm_dialog);
        TextView tv = findViewById(R.id.permission_guide_confirm_title);
        tv.setText(StringUtils.capitalizeFirstLetter(context.getText(R.string.promotion_enable_btn).toString()) + " " + context.getText(R.string.app_name));
        this.mCircleView = findViewById(R.id.permission_guide_confirm_ripple);
        this.mDialogBg = findViewById(R.id.permission_guide_confirm_dialog_bg);
        mPath = new Path();
        mRectf = new RectF();
    }

    public void setFingerView(View fingerView) {
        mFingerView = fingerView;
        initFingerViewLocation();
    }

    public void setShowConfirmDialog(boolean shown) {
        mIsShowConfirmDialog = shown;
    }

    private void initFingerViewLocation() {
        if (null == mFingerTagView) {
            return;
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Rect locationTag = ViewUtils.getLocationRect(mFingerTagView);
                Rect parentLocationTag = ViewUtils.getLocationRect(PermissionGuideView.this);
                int top = locationTag.top - parentLocationTag.top;
                int left = locationTag.left - parentLocationTag.left;
                ViewUtils.setMargins(mFingerView, left, top, 0, 0);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initFingerViewLocation();
    }

    public void startDoubleLayerAnim(boolean isFirstStart) {
        // Finger Appear
        ValueAnimator fingerAppearAnimator = null;
        if (isFirstStart) {
            fingerAppearAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            fingerAppearAnimator.setDuration(DURATION_APPEAR_OPEN);
            fingerAppearAnimator.setInterpolator(mDefaultInterpolator);
            fingerAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    PermissionGuideView.this.mFingerView.setTranslationX(Dimensions.getPhoneWidth(HSApplication.getContext()) * 114f / 1080 * (1 - floatValue));
                    PermissionGuideView.this.mFingerView.setTranslationY(Dimensions.getPhoneHeight(HSApplication.getContext()) * 78f / 1920 * (1 - floatValue));
                    PermissionGuideView.this.mFingerView.setAlpha(floatValue);
                }
            });
        }

        // Appear
        ValueAnimator appearAnimator = ValueAnimator.ofFloat(0.0f, 1f);
        appearAnimator.setDuration(isFirstStart ? 0 : DURATION_APPEAR_OPEN);
        appearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setAlpha(floatValue);
                PermissionGuideView.this.mScrollView.setAlpha(floatValue);
            }
        });

        // Translate scroll To Top
        ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0.0f, 120.0f * this.mDensity);
        scrollAnimator.setDuration(DURATION_FINGER_TRANSLATE_TO_TOP);
        scrollAnimator.setInterpolator(mDefaultInterpolator);
        scrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mScrollView.scrollTo(0, (int) floatValue);
                PermissionGuideView.this.mFingerView.setTranslationY(-floatValue);
            }
        });

        // Ripple Highlight
        ValueAnimator rippleHighLightAnimator = ValueAnimator.ofInt(0, 255);
        rippleHighLightAnimator.setDuration(DURATION_HIGH_LIGHT_RECTANGLE);
        rippleHighLightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PermissionGuideView.this.mRippleView.setHlAlpha((int) animation.getAnimatedValue());
            }
        });
        rippleHighLightAnimator.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                PermissionGuideView.this.mRippleView.performHighlight(true);
            }
        });

        // Finger Scroll To Down
        ValueAnimator fingerScrollDownAnimator = ValueAnimator.ofFloat(120.0f * this.mDensity, 50.0f * this.mDensity);
        fingerScrollDownAnimator.setDuration(DURATION_FINGER_TRANSLATE_TO_PRESS);
        fingerScrollDownAnimator.setInterpolator(mDefaultInterpolator);
        fingerScrollDownAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setTranslationY(-floatValue);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        if (isFirstStart) {
            animatorSet.playSequentially(fingerAppearAnimator, appearAnimator, scrollAnimator, rippleHighLightAnimator, fingerScrollDownAnimator);
        } else {
            animatorSet.playSequentially(appearAnimator, scrollAnimator, rippleHighLightAnimator, fingerScrollDownAnimator);
        }

        animatorSet.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!mAnimationsCancelled) {
                    PermissionGuideView.this.animToToggleView();
                }
            }
        });
        animatorSet.start();
        mAnimations = animatorSet;
    }

    public void startSingleLayerAnimation(boolean isFirstStart) {
        PermissionGuideView.this.mFrontViewToggleView.setVisibility(View.VISIBLE);
        this.mFrontViewToggleView.init();
        findViewById(R.id.permission_guide_icon_img).setVisibility(VISIBLE);
        ((ImageView) findViewById(R.id.scroll_view_mask_above)).setImageResource(R.drawable.permission_guide_mask_with_icon);
        ((ImageView) findViewById(R.id.scroll_view_mask_below)).setImageResource(R.drawable.permission_guide_mask_with_icon);

        // Finger Appear
        ValueAnimator fingerAppearAnimator = null;
        if (isFirstStart) {
            fingerAppearAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            fingerAppearAnimator.setDuration(DURATION_APPEAR_OPEN);
            fingerAppearAnimator.setInterpolator(mDefaultInterpolator);
            fingerAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    PermissionGuideView.this.mFingerView.setTranslationX(Dimensions.getPhoneWidth(HSApplication.getContext()) * 114f / 1080 * (1 - floatValue));
                    PermissionGuideView.this.mFingerView.setTranslationY(Dimensions.getPhoneHeight(HSApplication.getContext()) * 78f / 1920 * (1 - floatValue));
                    PermissionGuideView.this.mFingerView.setAlpha(floatValue);
                }
            });
        }

        // Appear
        ValueAnimator appearAnimator = ValueAnimator.ofFloat(0.0f, 1f);
        appearAnimator.setDuration(isFirstStart ? 0 : DURATION_APPEAR_OPEN);
        appearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setAlpha(floatValue);
                PermissionGuideView.this.mScrollView.setAlpha(floatValue);
            }
        });

        // Translate scroll To Top
        ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0.0f, 120.0f * this.mDensity);
        scrollAnimator.setDuration(DURATION_FINGER_TRANSLATE_TO_TOP);
        scrollAnimator.setInterpolator(mDefaultInterpolator);
        scrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mScrollView.scrollTo(0, (int) floatValue);
                PermissionGuideView.this.mFingerView.setTranslationY(-floatValue);
            }
        });

        // Ripple Highlight
        ValueAnimator rippleHighLightAnimator = ValueAnimator.ofInt(0, 255);
        rippleHighLightAnimator.setDuration(DURATION_HIGH_LIGHT_RECTANGLE);
        rippleHighLightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PermissionGuideView.this.mRippleView.setHlAlpha((int) animation.getAnimatedValue());
            }
        });
        rippleHighLightAnimator.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                PermissionGuideView.this.mRippleView.performHighlight(true);
            }
        });

        // Finger Scroll To Down
        ValueAnimator fingerScrollDownAnimator = ValueAnimator.ofFloat(120.0f * this.mDensity, 45.0f * this.mDensity);
        fingerScrollDownAnimator.setDuration(DURATION_FINGER_TRANSLATE_TO_PRESS);
        fingerScrollDownAnimator.setInterpolator(mDefaultInterpolator);
        fingerScrollDownAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setTranslationY(-floatValue);
            }
        });

        // Finger Scroll Turn On Button
        ValueAnimator fingerTurnOnAnimator = ValueAnimator.ofFloat(0, 1.0f);
        final float deltaFingerOpenSwitch = 15f * this.mDensity;
        fingerTurnOnAnimator.setDuration(DURATION_FINGER_OPEN_SWITCH);
        fingerTurnOnAnimator.setInterpolator(mDefaultInterpolator);
        fingerTurnOnAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = (float) valueAnimator.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setTranslationX(floatValue * deltaFingerOpenSwitch);

                float animatedFraction = valueAnimator.getAnimatedFraction();
                PermissionGuideView.this.mFrontViewToggleView.setCoefficient(animatedFraction);
            }
        });
        fingerTurnOnAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                PermissionGuideView.this.mFrontViewToggleView.startToggle();
            }

            public void onAnimationEnd(Animator animator) {
                PermissionGuideView.this.mFrontViewToggleView.stopToggle();
            }
        });

        // Finger disappear
        ValueAnimator fingerDisappearAnimator = ValueAnimator.ofFloat(0, 1.0f);
        fingerDisappearAnimator.setDuration(DURATION_FINGER_DISAPPEAR);
        fingerDisappearAnimator.setStartDelay(DELAY_FINGER_DISAPPEAR);
        fingerDisappearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                PermissionGuideView.this.mFingerView.setAlpha(1.0f - animatedFraction);
                PermissionGuideView.this.mRippleView.setHlAlpha((int) (1.0f - animatedFraction));
            }
        });
        fingerDisappearAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                PermissionGuideView.this.mFingerView.setTranslationX(0.0f);
                PermissionGuideView.this.mFingerView.setTranslationY(0.0f);
                if (!mIsShowConfirmDialog) {
                    PermissionGuideView.this.mScrollView.scrollTo(0, 0);
                }
            }
        });

        AnimatorSet dialogAnimatorSet = new AnimatorSet();
        if (mIsShowConfirmDialog) {
            // dialog appear
            ValueAnimator dialogAppearAnimator = ValueAnimator.ofFloat(0, 1.0f);
            dialogAppearAnimator.setDuration(DURATION_DIALOG_APPEAR);
            dialogAppearAnimator.setStartDelay(DELAY_FINGER_DISAPPEAR);
            dialogAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedFraction = animation.getAnimatedFraction();
                    PermissionGuideView.this.mDialogView.setAlpha(animatedFraction);
                    PermissionGuideView.this.mDialogBg.setAlpha(animatedFraction);
                    PermissionGuideView.this.mDialogView.setScaleX(0.8f + 0.2f * animatedFraction);
                    PermissionGuideView.this.mDialogView.setScaleY(0.8f + 0.2f * animatedFraction);
                }
            });
            dialogAppearAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    PermissionGuideView.this.mDialogView.setVisibility(VISIBLE);
                    PermissionGuideView.this.mDialogBg.setVisibility(VISIBLE);

                }

                public void onAnimationEnd(Animator animator) {
                }
            });

            // Finger appear
            ValueAnimator fingerOnDialogAppearAnimator = ValueAnimator.ofFloat(0, 1.0f);
            fingerOnDialogAppearAnimator.setDuration(DURATION_APPEAR_OPEN);
            fingerOnDialogAppearAnimator.setInterpolator(mAccelerateDecelerateInterpolator);
            fingerOnDialogAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    PermissionGuideView.this.mFingerView.setTranslationX(Dimensions.getPhoneWidth(HSApplication.getContext()) * 114f / 1080 * (0.8f - floatValue));
                    PermissionGuideView.this.mFingerView.setTranslationY(Dimensions.getPhoneHeight(HSApplication.getContext()) * 78f / 1920 * (0.50f - floatValue));
                    PermissionGuideView.this.mFingerView.setAlpha(floatValue);
                }
            });

            ValueAnimator rippleAppearAnimator = ValueAnimator.ofFloat(0, 1.0f);
            rippleAppearAnimator.setDuration(DURATION_RIPPLE_APPEAR);
            rippleAppearAnimator.setInterpolator(mDefaultInterpolator);
            rippleAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = Math.min((float) animation.getAnimatedValue() + 0.5f, 1f);
                    float alphaValue = (float) animation.getAnimatedValue();
                    if (alphaValue <= 0.4) {
                        alphaValue += 0.6;
                    } else {
                        alphaValue = 5 * (1 - alphaValue) / 3f;
                    }
                    mCircleView.setScaleX(floatValue);
                    mCircleView.setScaleY(floatValue);
                    mCircleView.setAlpha(alphaValue);
                }
            });

            // dialog appear
            ValueAnimator dialogDisappearAnimator = ValueAnimator.ofFloat(0, 1.0f);
            dialogDisappearAnimator.setDuration(DURATION_DIALOG_APPEAR);
            dialogDisappearAnimator.setStartDelay(DELAY_FINGER_DISAPPEAR);
            dialogDisappearAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedFraction = animation.getAnimatedFraction();
                    PermissionGuideView.this.mDialogView.setAlpha(1f - animatedFraction);
                    PermissionGuideView.this.mDialogBg.setAlpha(1f - animatedFraction);
                    PermissionGuideView.this.mFingerView.setAlpha(1.0f - animatedFraction);
                }
            });
            dialogDisappearAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    PermissionGuideView.this.mDialogView.setVisibility(GONE);
                    PermissionGuideView.this.mDialogBg.setVisibility(GONE);
                    PermissionGuideView.this.mFingerView.setTranslationX(0.0f);
                    PermissionGuideView.this.mFingerView.setTranslationY(0.0f);
                    PermissionGuideView.this.mScrollView.scrollTo(0, 0);
                }
            });

            dialogAnimatorSet.playSequentially(
                    dialogAppearAnimator,
                    fingerOnDialogAppearAnimator,
                    rippleAppearAnimator,
                    dialogDisappearAnimator
            );
        }

        // FirstView disappear
        ValueAnimator lastViewDisappearAnimator = ObjectAnimator.ofFloat(this.mScrollView, "alpha", 1.0f, 0.0f);
        lastViewDisappearAnimator.setDuration(DURATION_LAST_VIEW_DISAPPEAR);
        lastViewDisappearAnimator.setStartDelay(DELAY_LAST_VIEW_DISAPPEAR);
        lastViewDisappearAnimator.setInterpolator(mDefaultInterpolator);

        AnimatorSet standAnimatorSet = new AnimatorSet();
        standAnimatorSet.playSequentially(
                appearAnimator,
                scrollAnimator,
                rippleHighLightAnimator,
                fingerScrollDownAnimator,
                fingerTurnOnAnimator,
                fingerDisappearAnimator,
                dialogAnimatorSet
        );

        standAnimatorSet.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!mAnimationsCancelled) {
                    // mAnimations is nullified when view is detached from window, we should not start a new round here
                    PermissionGuideView.this.startSingleLayerAnimation(false);
                }
            }
        });

        if (isFirstStart) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(
                    fingerAppearAnimator, standAnimatorSet);
            animatorSet.start();
            mAnimations = animatorSet;
        } else {
            standAnimatorSet.start();
            mAnimations = standAnimatorSet;
        }
    }

    public void animToToggleView() {
        this.mLastView.setVisibility(View.VISIBLE);
        this.mLastView.bringToFront();
        this.mLastView.initToggle();
        this.mFingerView.bringToFront();
        this.mRippleView.performRipple(true);

        // Ripple click effect, High Light, LastView appear
        ValueAnimator rippleHighLightAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        rippleHighLightAnimator.setDuration(DURATION_FINGER_PRESSED);
        rippleHighLightAnimator.setInterpolator(mAccelerateDecelerateInterpolator);
        rippleHighLightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mRippleView.setFraction(floatValue);
            }
        });
        rippleHighLightAnimator.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                PermissionGuideView.this.mRippleView.performHighlight(false);
            }
        });

        // ScrollView disappear
        ValueAnimator scrollViewDisAppearAnimator = ValueAnimator.ofFloat(1.0f, 0f);
        scrollViewDisAppearAnimator.setDuration(DURATION_SCROLL_VIEW_DISAPPEAR);
        scrollViewDisAppearAnimator.setInterpolator(mDefaultInterpolator);
        scrollViewDisAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mScrollView.setAlpha(floatValue);
            }
        });

        // LastView appear
        ValueAnimator lastViewAppearAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        lastViewAppearAnimator.setDuration(DURATION_LAST_VIEW_APPEAR);
        lastViewAppearAnimator.setInterpolator(mDefaultInterpolator);
        lastViewAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (float) animation.getAnimatedValue();
                PermissionGuideView.this.mLastView.setAlpha(floatValue);
            }
        });
        lastViewAppearAnimator.addListener(new com.acb.libwallpaper.live.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                // Finger Scroll Top To Open Switch
                ValueAnimator fingerScrollTopAnimator = ValueAnimator.ofFloat(50.0f * PermissionGuideView.this.mDensity,
                        102.0f * PermissionGuideView.this.mDensity);
                fingerScrollTopAnimator.setDuration(DURATION_FINGER_TRANSLATE_TOP_TO_OPEN);
                fingerScrollTopAnimator.setInterpolator(mDefaultInterpolator);
                fingerScrollTopAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float floatValue = (float) animation.getAnimatedValue();
                        PermissionGuideView.this.mFingerView.setTranslationY(-floatValue);
                    }
                });
                fingerScrollTopAnimator.start();
            }
        });

        // Finger Scroll Turn On Button
        ValueAnimator fingerTurnOnAnimator = ValueAnimator.ofFloat(0, 1.0f);
        final float deltaFingerOpenSwitch = 15f * this.mDensity;
        fingerTurnOnAnimator.setDuration(DURATION_FINGER_OPEN_SWITCH);
        fingerTurnOnAnimator.setInterpolator(mDefaultInterpolator);
        fingerTurnOnAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = (float) valueAnimator.getAnimatedValue();
                PermissionGuideView.this.mFingerView.setTranslationX(floatValue * deltaFingerOpenSwitch);

                float animatedFraction = valueAnimator.getAnimatedFraction();
                PermissionGuideView.this.mLastViewToggleView.setCoefficient(animatedFraction);
            }
        });
        fingerTurnOnAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                PermissionGuideView.this.mLastViewToggleView.startToggle();
            }

            public void onAnimationEnd(Animator animator) {
                PermissionGuideView.this.mLastViewToggleView.stopToggle();
            }
        });

        // Finger disappear
        ValueAnimator fingerDisappearAnimator = ValueAnimator.ofFloat(0, 1.0f);
        fingerDisappearAnimator.setDuration(DURATION_FINGER_DISAPPEAR);
        fingerDisappearAnimator.setStartDelay(DELAY_FINGER_DISAPPEAR);
        fingerDisappearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                PermissionGuideView.this.mFingerView.setAlpha(1.0f - animatedFraction);
            }
        });
        fingerDisappearAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                PermissionGuideView.this.mFingerView.setTranslationX(0.0f);
                PermissionGuideView.this.mFingerView.setTranslationY(0.0f);
                PermissionGuideView.this.mScrollView.scrollTo(0, 0);
            }
        });

        // LastView disappear
        ValueAnimator lastViewDisappearAnimator = ObjectAnimator.ofFloat(this.mLastView, "alpha", 1.0f, 0.0f);
        lastViewDisappearAnimator.setDuration(DURATION_LAST_VIEW_DISAPPEAR);
        lastViewDisappearAnimator.setStartDelay(DELAY_LAST_VIEW_DISAPPEAR);
        lastViewDisappearAnimator.setInterpolator(mDefaultInterpolator);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(
                rippleHighLightAnimator,
                scrollViewDisAppearAnimator,
                lastViewAppearAnimator,
                fingerTurnOnAnimator,
                fingerDisappearAnimator,
                lastViewDisappearAnimator
        );
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!mAnimationsCancelled) {
                    // mAnimations is nullified when view is detached from window, we should not start a new round here
                    PermissionGuideView.this.startDoubleLayerAnim(false);
                }
            }
        });
        mAnimations = animatorSet;
        animatorSet.start();
    }

    public void onDismiss() {
        cancelAnimations();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimations();
    }

    private void cancelAnimations() {
        mAnimationsCancelled = true;
        if (mAnimations != null) {
            AnimatorSet animations = mAnimations;
            mAnimations = null;
            animations.cancel(); // onAnimationEnd is invoked in this call
        }

        mFingerView.setAlpha(0);
    }

    public void setTitleText(String title) {
        if (null != mTitleTv) {
            mTitleTv.setText(title);
        }
    }

    public void setContentText(String content) {
        if (null != mContentTv) {
            if (content.isEmpty())
                mContentTv.setVisibility(GONE);
            else
                mContentTv.setText(content);
        }
    }

    public void setLastTitleText(String lastTitleText) {
        if (null != mLastView) {
            mLastView.setLastTitleText(lastTitleText);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight()+ 50;
        mRectf.set(0,0,w,h);
        mPath.addRoundRect(mRectf, getResources().getDimension(R.dimen.dialog_corner_radius),
                getResources().getDimension(R.dimen.dialog_corner_radius), Path.Direction.CW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
    }
}
