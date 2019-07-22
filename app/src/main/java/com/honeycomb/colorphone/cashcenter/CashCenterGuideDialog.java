package com.honeycomb.colorphone.cashcenter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.boost.FloatWindowDialog;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FloatWindowMovableDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Commons;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.rom.RomUtils;

import java.util.Calendar;
import java.util.Random;

public class CashCenterGuideDialog extends FloatWindowMovableDialog {
    private final static String PREF_KEY_LAST_SHOW_CASH_GUIDE = "pref_key_last_show_cash_guide";
    private static final int DAY_MASK = 100;
    protected ViewGroup mContentView;

    private int[] backgroundImages = new int[] {
            R.drawable.cash_center_guide_image1,
            R.drawable.cash_center_guide_image2,
            R.drawable.cash_center_guide_image3
    };

    private int[] contentTexts = new int[] {
            R.string.cash_center_guide_content1,
            R.string.cash_center_guide_content2,
            R.string.cash_center_guide_content3,
            R.string.cash_center_guide_content4,
            R.string.cash_center_guide_content5,
            R.string.cash_center_guide_content6,
            R.string.cash_center_guide_content7,
            R.string.cash_center_guide_content8,
            R.string.cash_center_guide_content9,
            R.string.cash_center_guide_content10,
            R.string.cash_center_guide_content11,
            R.string.cash_center_guide_content12,
    };

    private int imageIndex;
    private int contentIndex;

    public static void showCashCenterGuideDialog(Context context) {
        FloatWindowDialog dialog = new CashCenterGuideDialog(context);
        FloatWindowManager.getInstance().showDialog(dialog);
    }

    public CashCenterGuideDialog(Context context) {
        this(context, null);
    }

    public CashCenterGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CashCenterGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Random random = new Random();

        imageIndex = random.nextInt(backgroundImages.length);
        contentIndex = random.nextInt(contentTexts.length);

        init();

        recordShow();

        isShown = true;
    }

    private void init() {
        mContentView = (ViewGroup) View.inflate(getContext(), R.layout.cash_center_text_guide, this);
        ImageView imageView = mContentView.findViewById(R.id.cash_center_guide_image);
        imageView.setImageResource(backgroundImages[imageIndex]);
        TextView textView = mContentView.findViewById(R.id.cash_center_guide_content);
        textView.setText(contentTexts[contentIndex]);
        mContentView.findViewById(R.id.cash_center_guide_close).setOnClickListener(v -> {
            dismiss();
            Analytics.logEvent("CashCenter_FloatingGuide_Close", "content", String.valueOf(contentIndex + 1));
        });

        viewViewWidth = Dimensions.pxFromDp(170);
        viewViewHeight = Dimensions.pxFromDp(70);
        viewOriginalX = Dimensions.getPhoneWidth(HSApplication.getContext()) - viewViewWidth;
    }

    @Override public boolean hasNoNeedToShow() {
        return !isShown;
    }

    @Override public void dismiss() {
        FloatWindowManager.getInstance().removeDialog(this);
        isShown = false;
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        mLayoutParams.type = getFloatWindowType();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.x = viewOriginalX;
        mLayoutParams.y = (int) (Dimensions.getPhoneHeight(getContext()) * .24f - Dimensions.pxFromDp(20));
        mLayoutParams.width = viewViewWidth;
        mLayoutParams.height = viewViewHeight;

        this.setLayoutParams(mLayoutParams);
        return mLayoutParams;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public void onClick() {
        ColorPhoneActivity.startColorPhone(getContext(), ColorPhoneActivity.CASH_POSITION);
        Analytics.logEvent("CashCenter_FloatingGuide_Click", "type", getPeriod(), "content", String.valueOf(contentIndex + 1));
        dismiss();
    }

    private void recordShow() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (hour >= 5 && hour < 8) {
            hour = 8;
        }

        if (hour >= 11 && hour < 14) {
            hour = 14;
        }

        if (hour >= 18 && hour < 21) {
            hour = 21;
        }

        Preferences.getDefault().putInt(PREF_KEY_LAST_SHOW_CASH_GUIDE, (day * DAY_MASK + hour));

        Analytics.logEvent("CashCenter_FloatingGuide_Show", "content", String.valueOf(contentIndex + 1));
    }

    public static boolean isPeriod(Context context) {
        if (!HSConfig.optBoolean(false, "Application", "CashCenter", "FloatingGuide")) {
            return false;
        }

        if (!RomUtils.checkIsHuaweiRom() && !RomUtils.checkIsMiuiRom()) {
            return false;
        }

        if (!Commons.isKeyguardLocked(context, false)
                && BaseKeyguardActivity.exist && !ScreenStatusReceiver.isScreenOn()) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int lastShow = Preferences.getDefault().getInt(PREF_KEY_LAST_SHOW_CASH_GUIDE, 0);
        int lastDay = lastShow / DAY_MASK;
        int lastHour = lastShow % DAY_MASK;
        if (lastDay < day) {
            lastHour = 0;
        }

        if (hour >= 5 && hour < 8 && hour > lastHour) {
            return true;
        }

        if (hour >= 11 && hour < 14 && hour > lastHour) {
            return true;
        }

        if (hour >= 18 && hour < 21 && hour > lastHour) {
            return true;
        }
        return false;
    }

    private String getPeriod() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 8) {
            return "5:00-8:00";
        }

        if (hour >= 11 && hour < 14) {
            return "11:00-14:00";
        }

        if (hour >= 18 && hour < 21) {
            return "18:00-21:00";
        }
        return "";
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {

    }
}
