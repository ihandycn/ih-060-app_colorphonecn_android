package com.honeycomb.colorphone.theme;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.ConfigChangeManager;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ThemeList {
    public static final String PREFS_THEME_APPLY = "theme_apply_array";
    private static final String PREFS_THEME_LIKE = "theme_like_array";

    private static final String TAG = ThemeList.class.getSimpleName();

    public ThemeList() {
        ConfigChangeManager.getInstance().registerCallbacks(ConfigChangeManager.AUTOPILOT, new ConfigChangeManager.Callback() {
            @Override
            public void onChange(int type) {
                if (type == ConfigChangeManager.AUTOPILOT) {
                    // Lifetime autopilot not handle it
                }
            }
        });
    }

    public void fillData(ArrayList<Theme> mRecyclerViewData) {
        final List<Theme> bgThemes = updateThemes(false);
        // Data ready
        mRecyclerViewData.clear();
        mRecyclerViewData.addAll(bgThemes);
    }

    @NonNull
    public static List<Theme> updateThemes(boolean onApplicationInit) {
        int selectedThemeId = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1);

        boolean isSpacialUser = RomUtils.checkIsMiuiRom() || RomUtils.checkIsVivoRom();
        boolean defaultTheme = HSConfig.optBoolean(false, "Application", "Theme", "DefaultTheme");
        boolean applyDefaultTheme = (selectedThemeId == -1) && (!isSpacialUser || defaultTheme);

        boolean autopilotRandomEnable = Ap.RandomTheme.enable();
        if (applyDefaultTheme) {
            selectedThemeId = autopilotRandomEnable ? Theme.RANDOM_THEME : Utils.getDefaultThemeId();
            HSLog.d("AP-ScreenFlash", "defaultThemeID : " + selectedThemeId);
            ThemePreviewView.saveThemeApplys(selectedThemeId);
            ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, selectedThemeId);
        }

        final List<Theme> bgThemes = new ArrayList<>(Theme.themes());

        String[] likeThemes = getThemeLikes();
        final int count = bgThemes.size();
        for (int i = 0; i < count; i++) {
            final Theme theme = bgThemes.get(i);
            theme.setSelected(false);
            // Like ?
            boolean isLike = isLikeTheme(likeThemes, theme.getValue());
            if (isLike) {
                theme.setDownload(theme.getDownload() + 1);
            }
            theme.setLike(isLike);
            // Selected ?
            if (theme.getId() == selectedThemeId) {
                theme.setSelected(true);
            }
        }

        Collections.sort(bgThemes, new Comparator<Theme>() {
            @Override
            public int compare(Theme o1, Theme o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

        final int idDefault = selectedThemeId;
        if (onApplicationInit) {
            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    updateThemeTasks(bgThemes, applyDefaultTheme, idDefault);
                    HSLog.d(TAG, "[Application init] Prepare theme list");
                }
            });
        } else {
            updateThemeTasks(bgThemes, applyDefaultTheme, idDefault);
        }
        return bgThemes;
    }

    private static void updateThemeTasks(List<Theme> bgThemes, boolean applyDefaultTheme, int idDefault) {
        boolean needPreload  = false;
        // Task update (if new theme added here, we update download task)
        for (Theme theme : bgThemes) {
            if (theme.isMedia() && !TasksManager.getImpl().checkTaskExist(theme)) {
                TasksManager.getImpl().addTask(theme);
                if (applyDefaultTheme && theme.getId() == idDefault) {
                    needPreload = true;
                }
            }
        }

        // Prepare default theme
        if (needPreload
                && idDefault != Utils.localThemeId) {
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    prepareThemeMediaFile(idDefault);
                }
            });
        }

        // Prepare next random theme
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (Ap.RandomTheme.enable()
                        && TasksManager.getImpl().isReady()) {
                    RandomTheme.getInstance().prepareNextTheme();
                }
            }
        });
    }

    private static void prepareThemeMediaFile(int idDefault) {
        HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "prepareThemeMediaFile");
        TasksManagerModel model = TasksManager.getImpl().getByThemeId(idDefault);
        TasksManager.doDownload(model, null);
    }

    private static boolean isLikeTheme(String[] likeThemes, int themeId) {
        for (String likeThemeId : likeThemes) {
            if (TextUtils.isEmpty(likeThemeId)) {
                continue;
            }
            if (themeId == Integer.parseInt(likeThemeId)) {
                return true;
            }
        }
        return false;
    }

    private static String[] getThemeLikes() {
        String likes = HSPreferenceHelper.getDefault().getString(PREFS_THEME_LIKE, "");
        return likes.split(",");
    }
}
