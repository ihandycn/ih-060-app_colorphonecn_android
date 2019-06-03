package colorphone.acb.com.libweather.launcher;

import android.text.Editable;
import android.text.TextWatcher;

public class PreventLeakTextWatcher implements TextWatcher {

    private LauncherTextWatcher mLauncherTextWatcher;

    public PreventLeakTextWatcher(LauncherTextWatcher launcherTextWatcher) {
        mLauncherTextWatcher = launcherTextWatcher;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (mLauncherTextWatcher != null) {
            mLauncherTextWatcher.beforeTextChanged(charSequence, i, i1, i2);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (mLauncherTextWatcher != null) {
            mLauncherTextWatcher.onTextChanged(charSequence, i, i1, i2);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mLauncherTextWatcher != null) {
            mLauncherTextWatcher.afterTextChanged(editable);
        }
    }

    public void release() {
        mLauncherTextWatcher = null;
    }
}
