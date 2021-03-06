package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

public class XiaomiRateGuideDialog extends FloatedRateGuideDialog {

    public static void show(Context context) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().showDialog(new XiaomiRateGuideDialog(context));
        } else {
            HSLog.i("XiaomiRateGuide", "XiaomiRateGuide not enable");
        }
    }

    public XiaomiRateGuideDialog(Context context) {
        this(context, null);
    }

    public XiaomiRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XiaomiRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
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
        content.setPadding(Dimensions.pxFromDp(25), Dimensions.pxFromDp(12.7f), Dimensions.pxFromDp(25), Dimensions.pxFromDp(26.7f));
    }
    @Override
    protected boolean hasWriteCommentIcon() {
        return false;
    }

    protected int getRateGuideContentString() {
        return R.string.xiaomi_rate_guide_content;
    }

    protected int getLayoutResId() {
        return R.layout.non_acc_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContent() {
        return R.id.non_acc_rate_guide_content;
    }

}
