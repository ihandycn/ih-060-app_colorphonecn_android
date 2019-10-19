package com.acb.libwallpaper.live.permission;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.base.BaseActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class FloatWindowPermissionGuide extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_window_guide);

        ImageView cancel = findViewById(R.id.cancel_btn);
        TextView gotIt = findViewById(R.id.ok_btn);
        gotIt.setBackground(BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor("#448AFF"),
                Dimensions.pxFromDp(6),
                true
        ));

        gotIt.setOnClickListener(view -> finish());
        cancel.setOnClickListener(view -> finish());
    }

}
