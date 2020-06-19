package com.acb.colorphone.guide;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.acb.colorphone.PermissionsManager;
import com.acb.colorphone.permissions.R;
import com.superapps.util.Bitmaps;
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
            guideMsg1Text.setText(getGuideMsg());

            TextView guideMsg2Text = view.findViewById(R.id.guide_msg_2);
            guideMsg2Text.setText(R.string.acb_phone_grant_accessibility_guide_msg_2_huawei);
        }
        setContentView(view, layoutParams);
        view.setOnClickListener(v -> finish());
    }

    private SpannableString getGuideMsg() {

        String msg = getString(R.string.acb_phone_grant_accessibility_guide_msg_1_huawei);
        int index = msg.indexOf("@");
        SpannableString spannableString = new SpannableString(msg);
        int drawableId = PermissionsManager.getInstance().getAppIcon();
        if (index < 0 || drawableId == 0) {
            return spannableString;
        }

        Bitmap bitmap = Bitmaps.decodeResourceWithFallback(getResources(), drawableId);
        bitmap = Bitmaps.getScaledBitmap(bitmap, Dimensions.pxFromDp(24), Dimensions.pxFromDp(24));
        ImageSpan imageSpan = new ImageSpan(this, bitmap, ImageSpan.ALIGN_BOTTOM);
        spannableString.setSpan(imageSpan, index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
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
