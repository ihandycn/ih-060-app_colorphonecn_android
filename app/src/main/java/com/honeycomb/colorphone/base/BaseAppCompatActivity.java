package com.honeycomb.colorphone.base;


import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.honeycomb.colorphone.util.ActivityUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class BaseAppCompatActivity extends HSAppCompatActivity {

    private static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityUtils.setNavigationBarDefaultColor(this);
    }

    public Fragment getCurrentDialogFragmentByTag(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }
}
