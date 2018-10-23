package com.honeycomb.colorphone.cashcenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class InnerCashGuideActivity extends AppCompatActivity {
    
    public static void start(Context context) {
        Intent starter = new Intent(context, InnerCashGuideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
