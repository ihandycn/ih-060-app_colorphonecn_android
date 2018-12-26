package com.honeycomb.colorphone.themeselector;

import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.call.assistant.ui.CallIdleAlertActivity;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class ThemeGuide {

    private static final String TAG = ThemeGuide.class.getSimpleName();
    private static final String LAST_GUIDE_THEME_ID_NAME = "last_guide_theme_id_name";
    private static final String LAST_GUIDE_THEME_TIME = "last_guide_theme_time";
    private static final String LAST_GUIDE_THEME_COUNT = "last_guide_theme_count";
    private static final String LAST_GUIDE_THEME_APPLY_TIME = "last_guide_theme_apply_time";

    private static ThemeGuide sThemeGuide;
    private final int sessionID;
    private boolean showThemeMain = false;
    private boolean showThemeDetail = false;

    private ThemeGuide() {
        sessionID = SessionMgr.getInstance().getCurrentSessionId();
    }

    public static void parser(View view) {
        sThemeGuide = new ThemeGuide();
        sThemeGuide.fillView(view);
        ThemeGuideTest.logThemeGuideShow();
    }

    public static int getInsteadLayoutID() {
        if (ThemeGuideTest.isThemeGuideShow()) {
            long lastShowTime = Preferences.getDefault().getLong(LAST_GUIDE_THEME_TIME, 0);
            long now = System.currentTimeMillis();
            boolean showInterval = (now - lastShowTime) > ThemeGuideTest.getInterval();

            int count = Preferences.getDefault().getInt(LAST_GUIDE_THEME_COUNT, 0);
            boolean showMax = true;
            if (DateUtils.isToday(lastShowTime)) {
                showMax = count < ThemeGuideTest.getMaxTime();
            } else {
                Preferences.getDefault().putInt(LAST_GUIDE_THEME_COUNT, 0);
            }

            int sucInterval = ThemeGuideTest.getIntervalAfterApplySuccess();
            long lastApply = Preferences.getDefault().getLong(LAST_GUIDE_THEME_APPLY_TIME, 0);
            boolean applySuccessInterval;
            if ((now - lastApply) > sucInterval * DateUtils.DAY_IN_MILLIS) {
                applySuccessInterval = true;
            } else if ((now - lastApply) < (sucInterval - 1) * DateUtils.DAY_IN_MILLIS) {
                applySuccessInterval = false;
            } else {
                applySuccessInterval = DateUtils.isToday(lastApply + sucInterval);
            }

            if (showInterval && showMax && applySuccessInterval) {
                Preferences.getDefault().putLong(LAST_GUIDE_THEME_TIME, now);
                Preferences.getDefault().incrementAndGetInt(LAST_GUIDE_THEME_COUNT);

                return R.layout.themes_instead_layout;
            }
        }
        return 0;
    }

    public static void logThemeDetailShow() {
        if (sThemeGuide == null) {
            return;
        }
        HSLog.i(TAG, "logThemeDetailShow d == " + sThemeGuide.showThemeDetail + "  m == " + sThemeGuide.showThemeMain
                + "  cID == " + SessionMgr.getInstance().getCurrentSessionId() + "  TGID == " + sThemeGuide.sessionID);
        if ((sThemeGuide.showThemeDetail || sThemeGuide.showThemeMain)
                && SessionMgr.getInstance().getCurrentSessionId() == sThemeGuide.sessionID) {
            ThemeGuideTest.logThemeGuideDetailShow();
        }
    }

    public static void logThemeApplied() {
        Preferences.getDefault().putLong(LAST_GUIDE_THEME_APPLY_TIME, System.currentTimeMillis());

        if (sThemeGuide == null) {
            return;
        }
        HSLog.i(TAG, "logThemeApplied d == " + sThemeGuide.showThemeDetail + "  m == " + sThemeGuide.showThemeMain
                + "  cID == " + SessionMgr.getInstance().getCurrentSessionId() + "  TGID == " + sThemeGuide.sessionID);
        if ((sThemeGuide.showThemeDetail || sThemeGuide.showThemeMain)
                && SessionMgr.getInstance().getCurrentSessionId() == sThemeGuide.sessionID) {
            ThemeGuideTest.logThemeGuideApply();
        }
    }

    private void fillView(View view) {
        ImageView[] themeImgs = new ImageView[3];
        themeImgs[0] = view.findViewById(R.id.theme_instead_theme1);
        themeImgs[1] = view.findViewById(R.id.theme_instead_theme2);
        themeImgs[2] = view.findViewById(R.id.theme_instead_theme3);

        View[] actions = new View[3];
        actions[0] = view.findViewById(R.id.theme_instead_theme_btn1);
        actions[1] = view.findViewById(R.id.theme_instead_theme_btn2);
        actions[2] = view.findViewById(R.id.theme_instead_theme_btn3);

        View action = view.findViewById(R.id.theme_instead_action_btn);
        action.setOnClickListener(v -> {
            sThemeGuide.showThemeMain = true;
            Navigations.startActivitySafely(view.getContext(), new Intent(view.getContext(), ColorPhoneActivity.class));
            ThemeGuideTest.logThemeGuideMoreClicked();
            HSGlobalNotificationCenter.sendNotification(CallIdleAlertActivity.FINISH_CALL_IDLE_ALERT_ACTIVITY);
        });

        List<Theme> guideThemes = getGuideThemes();
        if (guideThemes != null && guideThemes.size() >= 3) {
            int index = 0;
            boolean isShowApply = ThemeGuideTest.isApplyButtonShow();
            for (Theme theme : guideThemes) {
                fillThemeImage(themeImgs[index], theme);
                actions[index].setVisibility(isShowApply ? View.VISIBLE : View.GONE);
                index++;
            }
        }

    }

    private void fillThemeImage(ImageView iv, Theme theme) {
        Glide.with(iv.getContext())
                .asBitmap()
                .load(theme.getPreviewImage())
                .into(iv);
        iv.setOnClickListener(v -> {
            sThemeGuide.showThemeDetail = true;
            ThemePreviewActivity.start(iv.getContext(), theme.getIndex(), "ThemeGuide");
            ThemeGuideTest.logThemeGuideThemeClicked();
            HSGlobalNotificationCenter.sendNotification(CallIdleAlertActivity.FINISH_CALL_IDLE_ALERT_ACTIVITY);
        });
    }

    private List<Theme> getGuideThemes() {
        List allGuideThemesIds = HSConfig.getList("Application", "ThemeGuide");

        if (allGuideThemesIds != null && allGuideThemesIds.size() > 0) {
            String current = Ap.ScreenFlash.getDefaultThemeId();
            String last = getLastGuideTheme();
            List<String> guideThemeIds = new ArrayList<>(3);

            int size = allGuideThemesIds.size();
            int index = allGuideThemesIds.indexOf(last);
            String themeId = "";
            for (int i = index + 1; guideThemeIds.size() < 3; i++) {
                themeId = allGuideThemesIds.get(i % size).toString();
                if (!TextUtils.equals(current, themeId)) {
                    guideThemeIds.add(themeId);
                }
            }
            putLastGuideTheme(themeId);

            List<Theme> allThemes = Theme.themes();
            List<Theme> guideThemes = new ArrayList<>(3);
            for (String id : guideThemeIds) {
                for (Theme theme : allThemes) {
                    if (TextUtils.equals(id, theme.getIdName())) {
                        guideThemes.add(theme);
                    }
                }
            }
            HSLog.i(TAG, "guideThemesIDs == " + guideThemeIds);
            HSLog.i(TAG, "guideThemes == " + guideThemes);
            return guideThemes;
        }

        return null;
    }

    private String getLastGuideTheme() {
        return Preferences.getDefault().getString(LAST_GUIDE_THEME_ID_NAME, "");
    }

    private void putLastGuideTheme(String last) {
        Preferences.getDefault().putString(LAST_GUIDE_THEME_ID_NAME, last);
    }
}
