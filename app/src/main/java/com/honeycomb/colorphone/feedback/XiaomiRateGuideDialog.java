package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.util.AttributeSet;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

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
    protected boolean hasWriteCommentIcon() {
        return false;
    }

    protected int getRateGuideContentString() {
        return R.string.xiaomi_rate_guide_content;
    }

    protected int getLayoutResId() {
        return R.layout.xiaomi_rate_guide_layout;
    }

    @Override
    protected int getRateGuideContent() {
        return R.id.xiaomi_rate_guide_content;
    }

}
