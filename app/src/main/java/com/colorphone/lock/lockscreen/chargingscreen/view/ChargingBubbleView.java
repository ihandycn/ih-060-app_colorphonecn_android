package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhouzhenliang on 17/3/23.
 */

public class ChargingBubbleView extends View {

    private static final int MAX_BUBBLE_COUNT = 40;
    private static final int INTERVAL_FRAME_COUNT_ADD_BUBBLE = 6;
    private static final float RATIO_HORIZONTAL_SPREAD_EACH_UPDATE = 0.02f;

    private static class BubbleInfo {
        float radius;
        float posX;
        float posY;
        float furthestPosX;
        float velocity;
        float distance;
        float moved;
    }

    private float ratioBaseXXHDPI;

    private List<BubbleInfo> bubbleInfoList = new ArrayList<>();
    private ValueAnimator popupValueAnimator;
    private ValueAnimator stopValueAnimator;

    private Paint paint = new Paint();

    public ChargingBubbleView(Context context) {
        super(context);

        init();
    }

    public ChargingBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ChargingBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (BubbleInfo bubbleInfo : bubbleInfoList) {
            canvas.drawCircle(bubbleInfo.posX, bubbleInfo.posY, bubbleInfo.radius, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (popupValueAnimator != null) {
            popupValueAnimator.cancel();
        }

        if (stopValueAnimator != null) {
            stopValueAnimator.cancel();
        }
    }

    public void setPopupBubbleColor(int color) {
        paint.setColor(color);

        invalidate();
    }

    public void setPopupBubbleFlag(boolean isPopupBubble) {
        if (popupValueAnimator != null) {
            popupValueAnimator.cancel();
        }

        if (!isPopupBubble) {
            if (popupValueAnimator == null) {
                return;
            }

            if (stopValueAnimator == null) {
                stopValueAnimator = ValueAnimator.ofFloat(0f, 1f);
                stopValueAnimator.setDuration(10000);
                stopValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        updateBubbleData();
                        invalidate();
                    }
                });
            }

            stopValueAnimator.start();
            return;
        }

        if (popupValueAnimator == null) {
            popupValueAnimator = ValueAnimator.ofFloat(0f, 1f);
            popupValueAnimator.setDuration(2000);
            popupValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            popupValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                private int counter = 0;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (++counter % INTERVAL_FRAME_COUNT_ADD_BUBBLE == 0) {
                        addBubbleInfo();
                    }

                    updateBubbleData();
                    invalidate();
                }
            });
        }

        if (stopValueAnimator != null) {
            stopValueAnimator.cancel();
        }

        popupValueAnimator.start();
    }

    public void resumeAnim() {
        if (popupValueAnimator != null && !popupValueAnimator.isStarted()) {
            popupValueAnimator.start();
        }
    }

    public void pauseAnim() {
        if (popupValueAnimator != null) {
            popupValueAnimator.cancel();
        }
    }

    private void init() {
        ratioBaseXXHDPI = getResources().getDisplayMetrics().density / 3.0f;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(15, 190, 9));
    }

    private void addBubbleInfo() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        if (bubbleInfoList.size() >= MAX_BUBBLE_COUNT) {
            return;
        }

        BubbleInfo bubbleInfo = new BubbleInfo();

        Random random = new Random();
        float gaussian = (float) random.nextGaussian();
        while (gaussian < -1.0f || gaussian > 1.0f) {
            gaussian = (float) random.nextGaussian();
        }

        bubbleInfo.velocity = ((random.nextFloat() * (1.0f - 0.4f) + 0.4f) * 1.8f) * ratioBaseXXHDPI * 0.8f;
        bubbleInfo.distance = (random.nextFloat() * (280f - 10f) + 10f) * ratioBaseXXHDPI;

        bubbleInfo.radius = ((1 - Math.abs(gaussian)) * (9f - 1f) + 1f) * ratioBaseXXHDPI;
        bubbleInfo.posX = getWidth() / 2f;
        bubbleInfo.posY = getHeight() + bubbleInfo.radius * 2f;
        bubbleInfo.furthestPosX = getWidth() / 2f + gaussian * 32f - bubbleInfo.radius;

        bubbleInfoList.add(bubbleInfo);
    }

    private void updateBubbleData() {
        List<BubbleInfo> waitRemove = new ArrayList<>();

        final int halfWidth = getWidth() >> 1;
        for (BubbleInfo bubbleInfo : bubbleInfoList) {
            if (bubbleInfo.moved > bubbleInfo.distance) {
                waitRemove.add(bubbleInfo);
                continue;
            }

            if (bubbleInfo.furthestPosX < halfWidth) {
                if (bubbleInfo.posX > bubbleInfo.furthestPosX) {
                    bubbleInfo.posX += (bubbleInfo.furthestPosX - halfWidth) * RATIO_HORIZONTAL_SPREAD_EACH_UPDATE;
                }
            } else {
                if (bubbleInfo.posX < bubbleInfo.furthestPosX) {
                    bubbleInfo.posX += (bubbleInfo.furthestPosX - halfWidth) * RATIO_HORIZONTAL_SPREAD_EACH_UPDATE;
                }
            }

            bubbleInfo.posY -= bubbleInfo.velocity;
            bubbleInfo.moved += bubbleInfo.velocity;
        }

        bubbleInfoList.removeAll(waitRemove);
    }
}
