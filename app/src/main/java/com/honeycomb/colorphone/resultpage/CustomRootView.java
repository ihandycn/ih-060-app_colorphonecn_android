package com.honeycomb.colorphone.resultpage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.superapps.util.Dimensions;

public class CustomRootView extends FrameLayout {

    public CustomRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        } else {
//            this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        }
        View containerView = ViewUtils.findViewById(this, R.id.container_view);
        int bottomMargin = Dimensions.getNavigationBarHeight(getContext());
        setMargins(containerView, 0, 0, 0, bottomMargin);
    }


    public static void setMargins(View v, int l, int t, int r, int b) {
        if (null == v) {
            return;
        }
        if (v.getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams p = (MarginLayoutParams) v.getLayoutParams();
            boolean isRtl;
            if (v.isInEditMode()) {
                isRtl = false;
            } else {
                isRtl = Utils.isRtl();
            }
            p.setMargins(isRtl ? r : l, t, isRtl ? l : r, b);
            v.requestLayout();
        }
    }
}