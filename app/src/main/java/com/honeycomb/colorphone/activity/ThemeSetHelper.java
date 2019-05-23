package com.honeycomb.colorphone.activity;

import android.support.annotation.Nullable;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.ThemeEntry;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.Analytics;

import java.util.List;

public class ThemeSetHelper {

    private static List<ThemeEntry> sCacheContacntList;

    public static void onConfirm(List<ThemeEntry> themeEntries, Theme theme, @Nullable Runnable onCompleteCallback) {
        if (!ThemeGuide.isFromThemeGuide()) {
            Analytics.logEvent("Colorphone_SeletContactForTheme_Success",
                    "ThemeName", theme.getIdName(),
                    "SelectedContactsNumber", themeEntries.size() + "");
        }

        Analytics.logEvent("ThemeDetail_SetForContact_Success");
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

    public static void cacheContactList(List<ThemeEntry> contacts) {
        sCacheContacntList = contacts;
    }

    public static List<ThemeEntry> getCacheContactList() {
        return sCacheContacntList;
    }
}
