package com.honeycomb.colorphone.feedback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

@SuppressLint("ViewConstructor")
public class RateGuideDialogWithAccOppo2 extends RateGuideDialogWithAcc {

    private Rect rect;

    public static void show(Context context, Rect rect) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().removeAllDialogs();
            FloatWindowManager.getInstance().showDialog(new RateGuideDialogWithAccOppo2(context, rect));
        } else {
            HSLog.i("RateGuideWithAccXiaomi2", "RateGuideWithAccXiaomi2 not enable");
        }
    }

    public RateGuideDialogWithAccOppo2(Context context, Rect rect) {
        this(context, null, 0);
        this.rect = rect;
    }

    public RateGuideDialogWithAccOppo2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        View content = findViewById(getRateGuideContent());
        content.setBackgroundResource(R.drawable.five_star_rate_guide_bubble_right);
        content.setPadding(Dimensions.pxFromDp(26), Dimensions.pxFromDp(12.5f), Dimensions.pxFromDp(26), Dimensions.pxFromDp(26.7f));

        LayoutParams layoutParams = (LayoutParams) content.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        layoutParams.setMargins(0, 0, Dimensions.getPhoneWidth(getContext()) - rect.right+Dimensions.pxFromDp(10), Dimensions.getPhoneHeight(getContext()) - Dimensions.getNavigationBarHeight(getContext()) - rect.top+Dimensions.pxFromDp(10));
        content.setLayoutParams(layoutParams);
    }

    @Override
    protected boolean hasWriteCommentIcon() {
        return true;
    }

    protected int getLayoutResId() {
        return R.layout.acc_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContentString() {
        return R.string.rate_guide_content_with_acc_oppo_2;
    }

}
