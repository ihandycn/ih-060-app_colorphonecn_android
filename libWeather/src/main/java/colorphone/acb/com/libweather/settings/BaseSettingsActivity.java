package colorphone.acb.com.libweather.settings;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.MenuItem;

import colorphone.acb.com.libweather.util.ActivityUtils;


public abstract class BaseSettingsActivity extends BasePermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
//        LauncherActivityUtils.configSimpleAppBar(this, getString(getTitleId()), ContextCompat.getColor(this, R.color.material_text_black_primary), Color.WHITE, true);
        ActivityUtils.configStatusBarColor(this);
    }

    protected abstract @LayoutRes int getLayoutId();

    protected abstract @StringRes int getTitleId();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
