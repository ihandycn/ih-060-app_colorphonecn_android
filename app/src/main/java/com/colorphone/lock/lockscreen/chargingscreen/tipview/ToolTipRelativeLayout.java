package com.colorphone.lock.lockscreen.chargingscreen.tipview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ToolTipRelativeLayout extends RelativeLayout {

    public ToolTipRelativeLayout(final Context context) {
        super(context);
    }

    public ToolTipRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolTipRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public ToolTipView showToolTipForView(final ToolTip toolTip, final View view) {
        final ToolTipView toolTipView = new ToolTipView(getContext());
        addView(toolTipView);
        toolTipView.setToolTip(toolTip, view);

        return toolTipView;
    }
}
