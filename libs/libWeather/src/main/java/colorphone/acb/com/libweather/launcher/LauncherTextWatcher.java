package colorphone.acb.com.libweather.launcher;

import android.text.Editable;

public interface LauncherTextWatcher {

    void beforeTextChanged(CharSequence s, int start, int count, int after);

    void onTextChanged(CharSequence s, int start, int before, int count);

    void afterTextChanged(Editable s);
}
