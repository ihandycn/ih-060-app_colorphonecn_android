package com.honeycomb.colorphone.wallpaper.permission;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.LauncherAnalytics;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.base.BaseActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class NormalPermissionGuide extends BaseActivity {

    private String[] BoldContent = {
            HSApplication.getContext().getResources().getString(R.string.normal_permission_guide_permission),
            HSApplication.getContext().getResources().getString(R.string.permission_request_call),
            HSApplication.getContext().getResources().getString(R.string.permission_request_contacts)
    };

    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_permission_guide);

        LauncherAnalytics.logEvent("AuthorityPage_NotificationAccess_Guide_Show", true,
                "type", "Badge_UnreadMessage");
        ImageView cancel = findViewById(R.id.cancel_btn);
        TextView gotIt = findViewById(R.id.ok_btn);
        TextView description = findViewById(R.id.permission_guide_description);
        lottieAnimationView = findViewById(R.id.horizontal_top_lottie);

        gotIt.setBackground(BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor("#448AFF"),
                Dimensions.pxFromDp(6),
                true
        ));

        String des = getResources().getString(R.string.normal_permission_guide_dialog_content);
        Spannable span = new SpannableString(des);
        for (String s : BoldContent) {
            int start = des.indexOf(s);
            int end = start + s.length();
            if (start >= 0 && end <= span.length()) {
                span.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), start, end, 0);
                span.setSpan(new AbsoluteSizeSpan(Dimensions.pxFromDp(17)), start, end, 0);
                span.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
            }
        }

        description.setText(span, TextView.BufferType.SPANNABLE);

        gotIt.setOnClickListener(view -> finish());
        cancel.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        lottieAnimationView.resumeAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lottieAnimationView.pauseAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lottieAnimationView.cancelAnimation();
    }
}
