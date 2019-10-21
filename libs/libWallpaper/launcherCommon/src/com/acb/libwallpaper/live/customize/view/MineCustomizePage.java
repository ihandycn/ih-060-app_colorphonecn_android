package com.acb.libwallpaper.live.customize.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.customize.activity.Cc0ProtocolActivity;
import com.acb.libwallpaper.live.customize.activity.MyWallpaperActivity;
import com.acb.libwallpaper.live.customize.activity.UploadWallpaperActivity;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.update.UpdateUtils;
import com.acb.libwallpaper.live.util.Utils;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.acb.libwallpaper.live.view.WebViewActivity;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Fonts;
import com.superapps.util.Preferences;

import static com.acb.libwallpaper.live.customize.activity.Cc0ProtocolActivity.PREFS_KEY_CC0_USER_AGREED;

public class MineCustomizePage extends LinearLayout implements View.OnClickListener {

    public static final String PREF_KEY_UPDATE_ITEM_CLICKED = "wallpaper_update_item_clicked";

    public MineCustomizePage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setToolbarTitleWithoutLayoutParams(TextView titleTextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleTextView.setTextAppearance(R.style.ToolbarTextAppearance);
        } else {
            titleTextView.setTextAppearance(getContext(), R.style.ToolbarTextAppearance);
        }
        titleTextView.setTextSize(20);
        final Typeface typeface = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD);
        titleTextView.setTypeface(typeface);
    }


        @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView title = ViewUtils.findViewById(this, R.id.local_customize_title);
        setToolbarTitleWithoutLayoutParams(title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            title.setElevation(getContext().getResources().getDimensionPixelOffset(R.dimen.app_bar_elevation));
        }

        findViewById(R.id.my_wallpaper_cell).setOnClickListener(this);
        findViewById(R.id.upload_cell).setOnClickListener(this);
        findViewById(R.id.privacy_policy_cell_cell).setOnClickListener(this);
        findViewById(R.id.terms_of_service_cell).setOnClickListener(this);
        findViewById(R.id.update_cell).setOnClickListener(this);


        boolean needsUpdate = showShowUpdateBadge();
        TextView currentVersionLabel = (TextView) findViewById(R.id.current_version_label);
        if (needsUpdate) {
            String latestVersionName = HSConfig.optString("", "Application", "Update", "LatestVersionName");
            if (TextUtils.isEmpty(latestVersionName)) {
                latestVersionName = "--";
            }
            currentVersionLabel.setText(getResources().getString(R.string.launcher_settings_latest_version_detail, latestVersionName));
        } else {
            String currentVersionName = HSVersionControlUtils.getAppVersionName();
            currentVersionLabel.setText(getResources().getString(R.string.launcher_settings_current_version_detail, currentVersionName));
        }

        setupBadgesVisibility(needsUpdate);
    }

    /**
     * @param showShowUpdateBadge Passed to avoid repeated work only.
     *                            It's value should stay consistent with the return value of {@link #showShowUpdateBadge()}.
     */
    private void setupBadgesVisibility(boolean showShowUpdateBadge) {
        ImageView newVersionBadge = (ImageView) findViewById(R.id.new_update_available_badge);
        newVersionBadge.setVisibility(showShowUpdateBadge ? View.VISIBLE : View.INVISIBLE);
    }

    public static boolean showShowUpdateBadge() {
        int currentVersion = HSVersionControlUtils.getAppVersionCode();
        int latestVersion = Utils.getLatestVersionCode();
        boolean needsUpdate = currentVersion < latestVersion;
        boolean itemClicked = Preferences.get(LauncherFiles.DESKTOP_PREFS).getBoolean(PREF_KEY_UPDATE_ITEM_CLICKED, false);
        return needsUpdate && !itemClicked;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.my_wallpaper_cell) {
            getContext().startActivity(new Intent(getContext(), MyWallpaperActivity.class));
        } else if (i == R.id.upload_cell) {
            if (Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getBoolean(PREFS_KEY_CC0_USER_AGREED, false)) {
                getContext().startActivity(new Intent(getContext(), UploadWallpaperActivity.class));
            } else {
                getContext().startActivity(new Intent(getContext(), Cc0ProtocolActivity.class));
            }
        } else if (i == R.id.privacy_policy_cell_cell) {
            Intent privacyIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "PrivacyPolicyURL"),
                    false, false);
            getContext().startActivity(privacyIntent);
        } else if (i == R.id.terms_of_service_cell) {
            Intent termsOfServiceIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "TermsOfServiceURL"),
                    false, false);
            getContext().startActivity(termsOfServiceIntent);
        } else if (i == R.id.update_cell) {
            UpdateUtils.update((Activity) getContext());
            Preferences.get(LauncherFiles.DESKTOP_PREFS).putBoolean(PREF_KEY_UPDATE_ITEM_CLICKED, true);
            findViewById(R.id.new_update_available_badge).setVisibility(View.INVISIBLE);
        }
    }
}
