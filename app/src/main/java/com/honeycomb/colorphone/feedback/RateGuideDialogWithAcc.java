package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.util.AttributeSet;

import com.honeycomb.colorphone.R;

public abstract class RateGuideDialogWithAcc extends FloatedRateGuideDialog {

    public RateGuideDialogWithAcc(Context context) {
        this(context, null);
    }

    public RateGuideDialogWithAcc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RateGuideDialogWithAcc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected int getLayoutResId() {
        return R.layout.acc_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContent() {
        return R.id.acc_rate_guide_content;
    }

}
