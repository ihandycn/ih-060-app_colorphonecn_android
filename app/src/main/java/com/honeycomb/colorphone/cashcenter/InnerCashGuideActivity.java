package com.honeycomb.colorphone.cashcenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.honeycomb.colorphone.R;

public class InnerCashGuideActivity extends AppCompatActivity {
    
    public static void start(Context context) {
        Intent starter = new Intent(context, InnerCashGuideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cash_guide);

        TextView hintTv = findViewById(R.id.cash_title);
        String rawStr = getString(R.string.cash_guide_hint);
        int startIndex = rawStr.indexOf("0.25$");
        int endIndex = startIndex + "0.25$".length();
        SpannableString spannableString = SpannableString.valueOf(rawStr);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ffba00")),
                startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                );
        hintTv.setText(spannableString);

    }
}
