package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.view.CenterImageSpan;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

public class HuaweiRateGuideDialog extends BaseRateGuideDialog {

    public static void show(Context context) {
        if (HSConfig.optBoolean(true, "Application", "ShowStoreGuide")) {
            FloatWindowManager.getInstance().showDialog(new HuaweiRateGuideDialog(context));
        } else {
            HSLog.i("HuaweiRateGuide", "HuaweiRateGuide not enable");
        }
    }

    public HuaweiRateGuideDialog(Context context) {
        this(context, null);
    }

    public HuaweiRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HuaweiRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();

        TextView tv = findViewById(R.id.huawei_rate_guide_content);
        String content = getContext().getString(R.string.huawei_rate_guide_content);
        String replace = getContext().getString(R.string.replace_icon_text);
        SpannableString string = new SpannableString(content);
        int iconIndex = content.indexOf(replace);
        ImageSpan span = new CenterImageSpan(getContext(), R.drawable.huawei_rate_guide_image, Dimensions.pxFromDp(22));
        string.setSpan(span, iconIndex, iconIndex + replace.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setText(string);
    }

    protected int getLayoutResId() {
        return R.layout.huawei_rate_guide_layout;
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {
        View view = findViewById(R.id.huawei_rate_guide_content);
        view.setAlpha(0);
        view.animate().alpha(1).setDuration(200).start();
    }
}
