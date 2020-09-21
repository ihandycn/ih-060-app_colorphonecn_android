package com.acb.colorphone.permissions;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.rom.VivoUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class ContactVivoGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_permission_miui);
        RelativeLayout content = findViewById(R.id.container_view);
        if (content != null) {
            content.setBackgroundDrawable(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        String splitter = getString(R.string.acb_phone_permission_splitter);
        StringBuilder permission = new StringBuilder();
        if (!VivoUtils.checkWriteContactPermission(this)) {
            permission.append(getString(R.string.acb_phone_vivo_permission_write_contact));
        }

        if (!VivoUtils.checkReadContactPermission(this)) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_vivo_permission_read_contact));
        }

        if (!VivoUtils.checkReadCallLog(this)) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_vivo_permission_read_call_log));
        }

        if (!VivoUtils.checkStoragePermission()) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_vivo_permission_read_storage));
        }
        ((TextView) findViewById(R.id.content)).setText(permission);

        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xE6000000, Dimensions.pxFromDp(6), false));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }


}
