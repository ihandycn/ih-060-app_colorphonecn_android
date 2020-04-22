package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.util.AttributeSet;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

public class OppoRateGuideDialog extends FloatedRateGuideDialog {

    public static void show(Context context) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().showDialog(new OppoRateGuideDialog(context));
        } else {
            HSLog.i("OppoRateGuide", "OppoRateGuide not enable");
        }
    }

    public OppoRateGuideDialog(Context context) {
        this(context, null);
    }

    public OppoRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OppoRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected boolean hasWriteCommentIcon() {
        return true;
    }

    protected int getLayoutResId() {
        return R.layout.non_acc_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContent() {
        return R.id.non_acc_rate_guide_content;
    }

    @Override
    protected int getRateGuideContentString() {
        return R.string.oppo_rate_guide_content;
    }
}
