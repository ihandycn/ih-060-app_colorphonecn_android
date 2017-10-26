package com.honeycomb.colorphone.util;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.R;

public class ThemeUtils {

    public static void updateStyle(View container) {
        TextView name = (TextView) container.findViewById(R.id.caller_name);
        TextView number = (TextView) container.findViewById(R.id.caller_number);
        name.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR));
        number.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));

        name.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(2), Color.BLACK);
        number.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(1), Color.BLACK);
    }
}
