package com.acb.libwallpaper.live.base;


import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.acb.libwallpaper.live.util.ActivityUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class BaseAppCompatActivity extends HSAppCompatActivity {

    private static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityUtils.setNavigationBarDefaultColor(this);
    }

    public void showDialogFragment(DialogFragment dialogFragment) {
        if (isFinishing()) {
            return;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getCurrentDialogFragmentByTag(TAG_DIALOG_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
        }
        dialogFragment.show(ft, TAG_DIALOG_FRAGMENT);
    }

    public Fragment getCurrentDialogFragmentByTag(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }
}
