package com.honeycomb.colorphone.feedback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

@SuppressLint("ViewConstructor")
public class RateGuideDialogWithAcc1 extends RateGuideDialogWithAcc {

    private Rect rect;

    public static void show(Context context, Rect rect) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().showDialog(new RateGuideDialogWithAcc1(context, rect));
        } else {
            HSLog.i("RateGuideWithAcc1", "RateGuideWithAcc1 not enable");
        }
    }

    public RateGuideDialogWithAcc1(Context context, Rect rect) {
        this(context, null, 0);
        this.rect = rect;
    }

    public RateGuideDialogWithAcc1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    protected boolean hasWriteCommentIcon() {
        return false;
    }
    @Override public WindowManager.LayoutParams getLayoutParams() {

        mLayoutParams.type = getFloatWindowType();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;

        this.setLayoutParams(mLayoutParams);
        return mLayoutParams;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Threads.postOnMainThreadDelayed(this::dismiss,2200);

        View content = findViewById(getRateGuideContent());
        content.setBackgroundResource(R.drawable.five_star_rate_guide_bubble_middle);
        content.setPadding(Dimensions.pxFromDp(26), Dimensions.pxFromDp(12.5f), Dimensions.pxFromDp(26), Dimensions.pxFromDp(26.7f));

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) content.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, 0, 0, Dimensions.getPhoneHeight(getContext()) - Dimensions.getNavigationBarHeight(getContext()) - rect.top);
        content.setLayoutParams(layoutParams);

    }

    @Override
    protected int getRateGuideContentString() {
        return R.string.rate_guide_content_with_acc1;
    }

}
