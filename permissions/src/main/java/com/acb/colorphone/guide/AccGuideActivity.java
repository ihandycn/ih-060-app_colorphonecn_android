package com.acb.colorphone.guide;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.acb.colorphone.permissions.R;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;

public class AccGuideActivity extends Activity {

    public static void start(Context context) {
        Intent intent = new Intent(context, AccGuideActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

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
        if (Compats.IS_HUAWEI_DEVICE) {
            TextView guideMsg1Text = view.findViewById(R.id.guide_msg_1);
            guideMsg1Text.setText(R.string.acb_phone_grant_accessibility_guide_msg_1_huawei);

            TextView guideMsg2Text = view.findViewById(R.id.guide_msg_2);
            guideMsg2Text.setText(R.string.acb_phone_grant_accessibility_guide_msg_2_huawei);
        }
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
