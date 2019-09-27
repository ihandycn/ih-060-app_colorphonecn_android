package com.acb.colorphone.permissions;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class OverlayOppoGuideActivity extends AppCompatActivity {
    private HomeKeyWatcher homeKeyWatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_permission_miui);
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

        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xE6000000, Dimensions.pxFromDp(6), false));

        TextView title = findViewById(R.id.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            title.setText(getReplacedTitle(this, R.string.acb_phone_oppo_overlay_permission_guide_above_26));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title.setText(getReplacedTitle(this, R.string.acb_phone_oppo_overlay_permission_guide_above_24));
        } else {
            // do nothing
        }
        callback();

        homeKeyWatcher = new HomeKeyWatcher(this);
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override public void onHomePressed() {
                finish();
            }

            @Override public void onRecentsPressed() {
                finish();
            }
        });
        homeKeyWatcher.startWatch();
    }

    private void callback() {
        new Handler().postDelayed(() -> {
            try {
                ActivityManager am = ((ActivityManager) OverlayOppoGuideActivity.this.getSystemService(Context.ACTIVITY_SERVICE));
                am.moveTaskToFront(OverlayOppoGuideActivity.this.getTaskId(), 0);
                return;
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }, 900L);
    }

    public static CharSequence getReplacedTitle(Context context,int resId) {
        String descText = context.getString(resId);
        String icon_replace = context.getString(R.string.acb_app_icon_replace);
        if (descText.contains(icon_replace)) {
            int appIconIndex = descText.indexOf(icon_replace);
            if (appIconIndex >= 0) {
                int identifier = HSApplication.getContext().getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName());
                Drawable appIcon = ContextCompat.getDrawable(HSApplication.getContext(), identifier);
                if (appIcon != null) {
                    SpannableString highlighted = new SpannableString(descText);

                    int size = Dimensions.pxFromDp(24);
                    appIcon.setBounds(0, 0, size, size);
                    ImageSpan span = new ImageSpan(appIcon, ImageSpan.ALIGN_BOTTOM);
                    highlighted.setSpan(span, appIconIndex, appIconIndex + icon_replace.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    return highlighted;
                }
            }
        }
        return descText;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        homeKeyWatcher.stopWatch();
    }
}
