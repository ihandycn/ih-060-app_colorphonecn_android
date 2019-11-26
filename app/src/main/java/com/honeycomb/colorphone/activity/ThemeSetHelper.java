package com.honeycomb.colorphone.activity;

import android.support.annotation.Nullable;

import com.colorphone.ringtones.module.Ringtone;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactDBHelper;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.contact.ThemeEntry;
import com.honeycomb.colorphone.preview.ThemeStateManager;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class ThemeSetHelper {

    private static List<SimpleContact> sCacheContactList;

    public static void onConfirm(List<SimpleContact> contacts, Theme theme, @Nullable Runnable onCompleteCallback) {
        if (contacts == null) {
            return;
        }
        Analytics.logEvent("ColorPhone_Set_Successed", "SetType", "SetForSomeone",
                "Theme", theme.getName(),
                "SetFrom", ThemeStateManager.getInstance().getThemeModeName());

        ThemeApplyManager.getInstance().addAppliedTheme(theme.toPrefString());

        List<ThemeEntry> themeEntries = new ArrayList<>();
        for (SimpleContact c : contacts) {
            ContactDBHelper.Action action = ContactDBHelper.Action.INSERT;
            if (c.getThemeId() > 0) {
                action = ContactDBHelper.Action.UPDATE;
            }
            c.setThemeId(theme.getId());
            List<ThemeEntry> entries = ThemeEntry.valueOf(c, action);
            themeEntries.addAll(entries);
            // Clear status
            c.setSelected(false);
            ContactManager.getInstance().updateThemeId(c.getContactId(), theme.getId());
        }

        if (!ThemeGuide.isFromThemeGuide()) {
            Analytics.logEvent("Colorphone_SeletContactForTheme_Success",
                    "ThemeName", theme.getIdName(),
                    "SelectedContactsNumber", contacts.size() + "");
        }

        Analytics.logEvent("ThemeDetail_SetForContact_Success", "Category", ThemePreviewActivity.getCategoryId());
        ThemeGuide.logThemeApplied();
        Ap.DetailAd.onThemeChooseForOne();

        if (!themeEntries.isEmpty()) {
            ContactManager.getInstance().markDataChanged();
        }

        final int selectedCount = themeEntries.size();
        ContactManager.getInstance().updateDb(themeEntries, new Runnable() {
            @Override
            public void run() {

                if (selectedCount >= 1 && !themeEntries.isEmpty()) {
                    ThemeEntry themeEntry = themeEntries.get(0);
                    ShareAlertActivity.UserInfo userInfo = new ShareAlertActivity.UserInfo(themeEntry.getRawNumber(), themeEntry.getName(), themeEntry.getPhotoUri());
                }


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
                            Analytics.logEvent("ThemeWireAd_Show_FromThemeGuide");
                        }
                    }
                }

                if (onCompleteCallback != null) {
                    onCompleteCallback.run();
                }
            }
        });

    }

    public static void cacheContactList(List<SimpleContact> contacts) {
        sCacheContactList = contacts;
    }

    public static List<SimpleContact> getCacheContactList() {
        return sCacheContactList;
    }

    public static void setContactRingtone(Ringtone ringtone, List<SimpleContact> selectContacts) {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                for (SimpleContact contact : selectContacts) {
                    RingtoneHelper.setSingleRingtone(ringtone.getTitle(), ringtone.getFilePath(), String.valueOf(contact.getContactId()));
                }
            }
        });
    }
}
