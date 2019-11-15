package com.honeycomb.colorphone.wallpaper.base;

import android.app.DialogFragment;
import android.app.FragmentTransaction;

public class BaseDialogFragment extends DialogFragment {

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        transaction.add(this, tag);
        return transaction.commitAllowingStateLoss();
    }
}
