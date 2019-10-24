package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;

import com.ihs.app.framework.activity.HSAppCompatActivity;

public class UserInfoEditorActivity extends HSAppCompatActivity {
    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }
}
