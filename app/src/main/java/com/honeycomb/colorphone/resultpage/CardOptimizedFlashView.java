package com.honeycomb.colorphone.resultpage;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;

public class CardOptimizedFlashView extends View {

    private Bitmap bitmap;
    private Paint flashPaint;
    private RectF flashBound = new RectF();

    private int offsetX = -Utils.pxFromDp(25);
    private int offsetY = -Utils.pxFromDp(267);

    public CardOptimizedFlashView(Context context) {
        this(context, null);
    }

    public CardOptimizedFlashView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardOptimizedFlashView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void startFlashAnimation() {
        ValueAnimator animator = ValueAnimator.ofInt(-Utils.pxFromDp(267), getMeasuredHeight());
        animator.setDuration(660);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                offsetY = (int) animation.getAnimatedValue();
                CardOptimizedFlashView.this.invalidate();
            }
        });
        animator.start();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.result_page_card_optimized_light);

        flashPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        flashPaint.setAntiAlias(true);
        flashPaint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        flashBound.set(offsetX, offsetY, offsetX + bitmap.getWidth(), offsetY + bitmap.getHeight());
        canvas.drawBitmap(bitmap, null, flashBound, flashPaint);
    }
}
