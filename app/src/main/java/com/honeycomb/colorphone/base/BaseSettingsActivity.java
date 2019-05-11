package com.honeycomb.colorphone.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.ActivityUtils;

public abstract class BaseSettingsActivity extends BasePermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        ActivityUtils.configSimpleAppBar(this, getString(getTitleId()), ContextCompat.getColor(this, R.color.material_text_black_primary), Color.WHITE, true);

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
