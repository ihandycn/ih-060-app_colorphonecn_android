package com.honeycomb.colorphone.feedback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

@SuppressLint("ViewConstructor")
public class RateGuideDialogWithAccXiaomi2 extends RateGuideDialogWithAcc {

    private Rect rect;

    public static void show(Context context, Rect rect) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().showDialog(new RateGuideDialogWithAccXiaomi2(context, rect));
        } else {
            HSLog.i("RateGuideWithAccXiaomi2", "RateGuideWithAccXiaomi2 not enable");
        }
    }

    public RateGuideDialogWithAccXiaomi2(Context context, Rect rect) {
        this(context, null, 0);
        this.rect = rect;
    }

    public RateGuideDialogWithAccXiaomi2(Context context, AttributeSet attrs, int defStyleAttr) {
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
        content.setBackgroundResource(R.drawable.five_star_rate_guide_bubble_left);
        content.setPadding(Dimensions.pxFromDp(26), Dimensions.pxFromDp(12.5f), Dimensions.pxFromDp(26), Dimensions.pxFromDp(26.7f));

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) content.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
        layoutParams.setMargins(Dimensions.pxFromDp(72), 0, 0, Dimensions.getPhoneHeight(getContext()) - Dimensions.getNavigationBarHeight(getContext()) - rect.top - Dimensions.pxFromDp(92));
        content.setLayoutParams(layoutParams);
    }

    @Override
    protected boolean hasWriteCommentIcon() {
        return false;
    }

    protected int getLayoutResId() {
        return R.layout.acc_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContentString() {
        return R.string.rate_guide_content_with_acc_xiaomi_2;
    }

}
