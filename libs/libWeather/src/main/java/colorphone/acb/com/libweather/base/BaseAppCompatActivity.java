package colorphone.acb.com.libweather.base;


import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.ihs.app.framework.activity.HSAppCompatActivity;

import colorphone.acb.com.libweather.util.ActivityUtils;

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
