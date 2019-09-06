package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.view.CenterImageSpan;
import com.superapps.util.Dimensions;

public class HuaweiRateGuideDialog extends FullScreenDialog {

    public static void show(Context context) {
        FloatWindowManager.getInstance().showDialog(new HuaweiRateGuideDialog(context));
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

    @Override public void dismiss() {
        FloatWindowManager.getInstance().removeDialog(this);
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        mLayoutParams.type = getFloatWindowType();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;

        this.setLayoutParams(mLayoutParams);
        return mLayoutParams;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return super.onTouchEvent(event);
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
