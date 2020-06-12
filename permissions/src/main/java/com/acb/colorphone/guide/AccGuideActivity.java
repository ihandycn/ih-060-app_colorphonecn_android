package com.acb.colorphone.guide;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.acb.colorphone.permissions.R;
import com.superapps.util.Dimensions;

public class AccGuideActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        ActivityHook.m15071a(this);
        super.onCreate(bundle);

        initView();
    }

    private void initView() {
        getWindow().getDecorView().setPadding(Dimensions.pxFromDp(7), 0, Dimensions.pxFromDp(7), 0);
        LayoutParams layoutParams = getWindow().getAttributes();
        getWindow().setFlags(32, 32);
        layoutParams.gravity = 48;
        layoutParams.width = -1;

        View view = View.inflate(this, R.layout.activity_acc_guide, null);
        setContentView(view, layoutParams);
        view.setOnClickListener(v -> finish());
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}
