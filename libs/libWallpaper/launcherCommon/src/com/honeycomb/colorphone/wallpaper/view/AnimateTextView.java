package com.honeycomb.colorphone.wallpaper.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.animation.LauncherAnimUtils;
import com.superapps.view.TypefacedTextView;

public class AnimateTextView extends TypefacedTextView {

    private int prevAlpha;
    private int nextAlpha;
    private int divIndex;

    public AnimateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void appear(boolean animated) {
        setVisibility(VISIBLE);
        divIndex = 0;
        if (animated) {
            prevAlpha = 0x00;
            nextAlpha = 0x00;
            ValueAnimator animator = LauncherAnimUtils.ofFloat(this, 0, 1);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    prevAlpha = (int) (valueAnimator.getAnimatedFraction() * 0x99);
                    divIndex = (int) (valueAnimator.getAnimatedFraction() * getText().length());
                    invalidate();
                }
            });
            animator.setDuration(500);
            animator.start();
        } else {
            prevAlpha = 0x00;
            nextAlpha = 0x99;
            invalidate();
        }
    }

    public void disappear(boolean animated) {
        divIndex = 0;
        if (animated) {
            prevAlpha = 0x00;
            nextAlpha = 0x99;
            ValueAnimator animator = LauncherAnimUtils.ofFloat(this, 0, 1);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    nextAlpha = (int) ((1 - valueAnimator.getAnimatedFraction()) * 0x99);
                    divIndex = (int) (valueAnimator.getAnimatedFraction() * getText().length());
                    invalidate();
                }
            });
            animator.setDuration(500);
            animator.start();
        } else {
            prevAlpha = 0x00;
            nextAlpha = 0x99;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();

        paint.setColor(getResources().getColor(R.color.moment_header_greetings_txt_color));
        paint.setAlpha(prevAlpha);
        int length = getText().length();
        canvas.drawText(getText().subSequence(0, divIndex).toString(), getPaddingLeft(),
                (getPaddingTop() + canvas.getHeight() - (paint.descent() + paint.ascent())) / 2,
                paint);

        paint.setAlpha(nextAlpha);
        canvas.drawText(getText().subSequence(divIndex, length).toString(), getPaddingLeft() + paint.measureText(getText(), 0, divIndex),
                (getPaddingTop() + canvas.getHeight() - (paint.descent() + paint.ascent())) / 2,
                paint);
    }
}
