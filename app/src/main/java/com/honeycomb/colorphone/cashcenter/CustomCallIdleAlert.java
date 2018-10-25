package com.honeycomb.colorphone.cashcenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.call.assistant.ui.CallIdleAlert;
import com.call.assistant.ui.CallIdleAlertActivity;
import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

@SuppressLint("ViewConstructor")
public class CustomCallIdleAlert extends CallIdleAlert {

    private LottieAnimationView lottieAnimationView;
    private TextView title;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isAnimating = false;

    public CustomCallIdleAlert(Context context, CallIdleAlertActivity.Data data) {
        super(context, data);
        initCashTips();
    }

    private void initCashTips() {
        CashUtils.Event.onGuideViewShow(CashUtils.Source.CallAlertFloatBar);
        ViewGroup container = findViewById(R.id.top_extra_container);

        View cashTipRoot = LayoutInflater.from(getContext()).inflate(R.layout.cashcenter_tip_for_call_alert, container, false);
        container.addView(cashTipRoot);

        cashTipRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CashUtils.Event.onGuideViewClick(CashUtils.Source.CallAlertFloatBar);
                CashUtils.startWheelActivity(null, CashUtils.Source.CallAlertFloatBar);

            }
        });
        title = cashTipRoot.findViewById(R.id.call_extra_cash_title);
        String rawStr= getResources().getString(R.string.cash_assistant_guide_hint);
        int startIndex = rawStr.indexOf("0.25$");
        int endIndex = startIndex + "0.25$".length();
        SpannableString spannableString = SpannableString.valueOf(rawStr);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ffba00")),
                startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        title.setText(spannableString);
        title.setVisibility(INVISIBLE);

        lottieAnimationView =
                cashTipRoot.findViewById(R.id.call_extra_cash_icon);


    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            lottieAnimationView.playAnimation();
            mHandler.post(() -> doPopAnimationShow());
        }
    }

    private void doPopAnimationShow() {
        if (isAnimating) {
            return;
        }
        int x = title.getWidth();
        int y = title.getHeight();
        title.setPivotX(x);
        title.setPivotY(y/2);
        title.setScaleX(0.1f);
        title.setScaleY(0.1f);
        title.setVisibility(VISIBLE);
        HSLog.d("Custom-Cash-tip",  "doPopAnimationShow," + "x = " + x + ",y = " + y);

        title.animate().scaleX(1).scaleY(1)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

}
