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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class ContactMIUIGuideActivity extends AppCompatActivity {

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

        setMIUI6StatusBarDarkMode(this, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        String splitter = getString(R.string.acb_phone_permission_splitter);
        StringBuilder permission = new StringBuilder();
        if (RuntimePermissions.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != RuntimePermissions.PERMISSION_GRANTED) {
            permission.append(getString(R.string.acb_phone_permission_write_contact));
        }

        if (RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != RuntimePermissions.PERMISSION_GRANTED) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_permission_read_contact));
        }

        if (RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != RuntimePermissions.PERMISSION_GRANTED) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_permission_read_call_log));
        }

        if (RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != RuntimePermissions.PERMISSION_GRANTED) {
            if (permission.length() != 0) {
                permission.append(splitter);
            }
            permission.append(getString(R.string.acb_phone_permission_read_storage));
        }
        ((TextView) findViewById(R.id.content)).setText(permission);

        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xE6000000, Dimensions.pxFromDp(6), false));
    }

    private static void setMIUI6StatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }


}
