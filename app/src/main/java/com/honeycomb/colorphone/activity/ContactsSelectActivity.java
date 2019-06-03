package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactDBHelper;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.contact.ThemeEntry;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Activity销毁 状态保存和恢复
 */

public class ContactsSelectActivity extends ContactsActivity {


    private Theme mTheme;
    ContactManager.LoadCallback mCallback = new ContactManager.LoadCallback() {
        @Override
        public void onLoadFinish() {
            onContactsDataReady(ContactManager.getInstance().getThemes(false));
            updateSelectMode(true);
            setHeaderHint(getString(R.string.contact_select_hint, mTheme.getName()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTheme = (Theme) getIntent().getSerializableExtra(EXTRA_THEME);
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.select_contacts);

        ContactManager.getInstance().register(mCallback);

    }

    @Override
    protected void onDestroy() {
        ContactManager.getInstance().unRegister(mCallback);
        super.onDestroy();
    }

    @Override
    protected void onConfirmed(List<SimpleContact> contacts) {

        final List<ThemeEntry> themeEntries = new ArrayList<>();

        int contactSelectedCount = 0;
        for (SimpleContact c : contacts) {
            if (c.isSelected()) {
                contactSelectedCount++;
                ContactDBHelper.Action action = ContactDBHelper.Action.INSERT;
                if (c.getThemeId() > 0) {
                    action = ContactDBHelper.Action.UPDATE;
                }

                recordAppliedTheme(c, mTheme.getIdName());

                c.setThemeId(mTheme.getId());
                List<ThemeEntry> entries = ThemeEntry.valueOf(c, action);
                themeEntries.addAll(entries);

                // Clear status
                c.setSelected(false);
            }
        }

        if (!ThemeGuide.isFromThemeGuide()) {
            LauncherAnalytics.logEvent("Colorphone_SeletContactForTheme_Success",
                    "ThemeName", mTheme.getIdName(),
                    "SelectedContactsNumber", themeEntries.size() + "");
        }
        int fromType = getIntent().getIntExtra(INTENT_KEY_FROM_TYPE, FROM_TYPE_MAIN);
        if (fromType == FROM_TYPE_POPULAR_THEME) {
            LauncherAnalytics.logEvent("Colorphone_BanboList_ThemeDetail_SeletContactForTheme_Success");
        } else {
            LauncherAnalytics.logEvent("Colorphone_MainView_ThemeDetail_SeletContactForTheme_Success");
            LauncherAnalytics.logEvent("ColorPhone_ThemeDetail_SetForContact_Success", LauncherAnalytics.FLAG_LOG_FIREBASE);
        }
        ThemeGuide.logThemeApplied();
        Ap.DetailAd.onThemeChooseForOne();
        Ap.RandomTheme.logEvent("detail_page_setforcontact_success");
        LauncherAnalytics.logEvent("detail_page_setforcontact_success_round2");

        if (!themeEntries.isEmpty()) {
            ContactManager.getInstance().markDataChanged();
        }

        final int selectedCount = contactSelectedCount;
        ContactManager.getInstance().updateDb(themeEntries, new Runnable() {
            @Override
            public void run() {

                if (selectedCount >= 1 && !themeEntries.isEmpty()) {
                    ThemeEntry themeEntry = themeEntries.get(0);
                    ShareAlertActivity.UserInfo userInfo = new ShareAlertActivity.UserInfo(themeEntry.getRawNumber(), themeEntry.getName(), themeEntry.getPhotoUri());
                    NotificationUtils.logThemeAppliedFlurry(mTheme);
                    if (GuideApplyThemeActivity.start(ContactsSelectActivity.this, true, userInfo, selectedCount >= 2)) {
                        ContactsSelectActivity.this.finish();
                        return;
                    }
                }
                Utils.showToast(getString(R.string.apply_success));
                ContactsSelectActivity.this.finish();

                if (ConfigSettings.showAdOnApplyTheme()) {
                    if (!ThemeGuide.isFromThemeGuide()) {
                        Ap.DetailAd.logEvent("colorphone_seletcontactfortheme_ad_should_show");
                    }
                    boolean show = AdManager.getInstance().showInterstitialAd();
                    if (show) {
                        if (!ThemeGuide.isFromThemeGuide()) {
                            Ap.DetailAd.logEvent("colorphone_seletcontactfortheme_ad_show");
                        }
                        if (ThemeGuide.isFromThemeGuide()) {
                            LauncherAnalytics.logEvent("ColorPhone_ThemeWireAd_Show_FromThemeGuide");
                        }
                    }
                }
            }
        });

    }

    private void recordAppliedTheme(SimpleContact contact, String idName) {
        if (contact == null) {
            return;
        }
        ThemeRecommendManager.getInstance().putAppliedTheme(contact.getRawNumber(), idName);
        List<String> otherNumbers = contact.getOtherNumbers();
        if (otherNumbers != null) {
            for (String number : otherNumbers) {
                ThemeRecommendManager.getInstance().putAppliedTheme(number, idName);
            }
        }
    }

    @Override
    protected void onConfigConfirmButton(Button confirmButton) {
        confirmButton.setEnabled(false);
        confirmButton.setBackgroundResource(R.drawable.btn_bg_yellow_disabled);
        confirmButton.setText(R.string.set);
    }

    @Override
    protected void onConfigActionView(TextView actionText) {
        actionText.setVisibility(View.GONE);
    }
}
