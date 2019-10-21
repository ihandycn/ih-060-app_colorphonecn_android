package com.acb.libwallpaper.live.base;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.acb.libwallpaper.live.util.ActivityUtils;
import com.ihs.app.framework.activity.HSActivity;

public class BaseActivity extends HSActivity {

    private static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";
    protected boolean mIsSetNavigationBarColor = true;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIsSetNavigationBarColor) {
            ActivityUtils.setNavigationBarDefaultColor(this);
        }

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
        ft.addToBackStack(null);
        dialogFragment.show(ft, TAG_DIALOG_FRAGMENT);
    }

    public Fragment getCurrentDialogFragmentByTag(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

}
