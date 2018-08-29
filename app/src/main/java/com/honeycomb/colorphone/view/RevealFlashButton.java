package com.honeycomb.colorphone.view;

/**
 * Created by ihandysoft on 2017/7/17.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;

public class RevealFlashButton extends AppCompatButton {

    private static final float REVEAL_START_SCALE_X = 0.1f;

    private long mRevealDuration;
    private long mFlashDuration;

    private int mLightWidth;
    private int mTotalTranslation;


    Animator mFlashAnimation;
    float mLightTranslateProgress = -1f;

    private Paint mLightPaint;
    private Bitmap mLight;
    private Rect mRect = new Rect();

    public RevealFlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        mRevealDuration = res.getInteger(R.integer.config_resultPageActionButtonRevealDuration);
        mFlashDuration = res.getInteger(R.integer.config_resultPageActionButtonFlashDuration);

        if (!isInEditMode()) {
            setVisibility(INVISIBLE);
        }
        setTypeface(Typeface.DEFAULT);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RevealFlashButton);
        float lightAlpha = a.getFloat(R.styleable.RevealFlashButton_flashLightAlpha, 1.0f);
        a.recycle();

        mLightPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mLightPaint.setAlpha((int) (lightAlpha * 0xff));
        mLightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mLight = Utils.decodeResourceWithFallback(res, R.drawable.acb_phone_action_button_flash);
    }

    public void reveal() {
        setScaleX(REVEAL_START_SCALE_X);
        setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(REVEAL_START_SCALE_X, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setScaleX((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.setDuration(mRevealDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public long getRevealDuration() {
        return mRevealDuration;
    }

    public void setRevealDuration(long revealDuration) {
        mRevealDuration = revealDuration;
    }

    public long getFlashDuration() {
        return mFlashDuration;
    }

    public void setFlashDuration(long flashDuration) {
        mFlashDuration = flashDuration;
    }

    public void revealWithoutAnimator() {
        setVisibility(VISIBLE);
    }

    public void flash() {
        mLightWidth = (int) (getHeight() * ((float) mLight.getWidth()  / mLight.getHeight()));
        mTotalTranslation = getWidth() + mLightWidth;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLightTranslateProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLightTranslateProgress = -1f;
                mFlashAnimation = null;
                invalidate();
            }
        });
        animator.setDuration(mFlashDuration);
        animator.setInterpolator(PathInterpolatorCompat.create(.45f, .87f, .76f, .88f));
        animator.start();
        mFlashAnimation = animator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        if (mLightTranslateProgress >= 0f) {
            drawFlashLight(canvas);
        }
    }

    private void drawFlashLight(Canvas canvas) {
        int right = (int) (mLightTranslateProgress * mTotalTranslation);
        mRect.set(right - mLightWidth, 0, right, getHeight());
        canvas.drawBitmap(mLight, null, mRect, mLightPaint);
    }
}
