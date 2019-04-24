package com.honeycomb.colorphone.startguide;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SetAsDialerDialog extends FullScreenDialog {
    public SetAsDialerDialog(Context context) {
        this(context, null);
    }

    public SetAsDialerDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetAsDialerDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPage();
    }

    private void initPage() {

        ConfigEvent.guideShow();

        Button actionBtn = findViewById(R.id.guide_action);
        actionBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_BOLD));
        actionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#dcdcdc"),
                Color.parseColor("#55000000"),
                Dimensions.pxFromDp(22),
                false,
                true));
        actionBtn.setOnClickListener(v ->
        {
            ConfigEvent.guideConfirmed();
            DefaultPhoneUtils.checkDefaultPhoneSettings();
            dismiss();
        });

        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigEvent.guideClose();
                dismiss();
            }
        });
    }

    @Override protected int getLayoutResId() {
        return R.layout.guide_set_default_phone;
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
       WindowManager.LayoutParams lp = return super.getLayoutParams();
       lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {

    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return super.IsInitStatusBarPadding();
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return true;
    }

}
