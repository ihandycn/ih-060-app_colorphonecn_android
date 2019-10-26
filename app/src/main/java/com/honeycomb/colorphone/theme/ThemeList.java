package com.honeycomb.colorphone.theme;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.util.ColorPhoneCrashlytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import hugo.weaving.DebugLog;

public class ThemeList {
    public static final String PREFS_THEME_APPLY = "theme_apply_array";
    private static final String PREFS_THEME_LIKE = "theme_like_array";

    private static final String TAG = ThemeList.class.getSimpleName();

    private static final boolean DEBUG_THEME_CHANGE = BuildConfig.DEBUG & false;

    private static ThemeList INSTANCE = new ThemeList();

    private Theme mThemeNone;
    private final ArrayList<Theme> themes = new ArrayList<>(30);

    private ThemeData mainFrameThemeData;
    private ThemeDataForUser uploadThemeData;
    private ThemeDataForUser publishThemeData;


    private Handler mTestHandler = new Handler(Looper.getMainLooper());
    private Runnable sTestRunnable = new Runnable() {
        @Override
        public void run() {
            Iterator<Theme> iter = themes.iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
                HSLog.d("THEME", "Test size --, current size = " + themes.size());
                mTestHandler.postDelayed(this, 4000);
            }

        }
    };

    private ThemeList() {

    }

    public static ThemeList getInstance() {
        return INSTANCE;
    }

    /**
     * @param isRefresh refresh or loadMore
     */
    public void requestThemeForMainFrame(boolean isRefresh, ThemeUpdateListener listener) {
        int pageIndex;
        if (isRefresh) {
            if (mainFrameThemeData == null) {
                mainFrameThemeData = new ThemeData();
            } else {
                mainFrameThemeData.clear();
            }
            pageIndex = mainFrameThemeData.getPageIndex();
        } else {
            pageIndex = mainFrameThemeData.getPageIndex() + 1;
        }

        HttpManager.getInstance().getAllThemes(pageIndex, new Callback<AllThemeBean>() {

            @Override
            public void onFailure(String errorMsg) {
                listener.onFailure(errorMsg);
            }

            @Override
            public void onSuccess(AllThemeBean allThemeBean) {
                if (allThemeBean != null && allThemeBean.getShow_list() != null && allThemeBean.getShow_list().size() > 0) {

                    mainFrameThemeData.setPageIndex(allThemeBean.getPage_index());
                    if (isRefresh) {
                        mainFrameThemeData.setThemeList(Theme.transformData(0, allThemeBean));
                    } else {
                        mainFrameThemeData.appendTheme(Theme.transformData(mainFrameThemeData.getThemeSize(), allThemeBean));
                    }

                    listener.onSuccess(true);
                } else {
                    listener.onSuccess(false);
                }
            }
        });
    }

    public void requestThemeForUserUpload(boolean isRefresh, ThemeUpdateListener listener) {
        int pageIndex;
        if (isRefresh) {
            if (uploadThemeData == null) {
                uploadThemeData = new ThemeDataForUser();
            } else {
                uploadThemeData.clear();
            }
            pageIndex = uploadThemeData.getPageIndex();
        } else {
            pageIndex = uploadThemeData.getPageIndex() + 1;
        }

        HttpManager.getInstance().getUserUploadedVideos(pageIndex, new Callback<AllUserThemeBean>() {

            @Override
            public void onFailure(String errorMsg) {
                listener.onFailure(errorMsg);
            }

            @Override
            public void onSuccess(AllUserThemeBean allUserThemeBean) {
                if (allUserThemeBean != null && allUserThemeBean.getShow_list() != null && !allUserThemeBean.getShow_list().isEmpty()) {
                    uploadThemeData.setPageIndex(allUserThemeBean.getPage_index());
                    ArrayList<Theme> dataList = Theme.transformData(allUserThemeBean);
                    if (isRefresh) {
                        uploadThemeData.updateData(dataList);
                    } else {
                        uploadThemeData.appendData(dataList);
                    }

                    listener.onSuccess(true);
                } else {
                    listener.onSuccess(false);
                }
            }
        });
    }

    public void requestThemeForUserPublish(boolean isRefresh, ThemeUpdateListener listener) {
        int pageIndex;
        if (isRefresh) {
            if (publishThemeData == null) {
                publishThemeData = new ThemeDataForUser();
            } else {
                publishThemeData.clear();
            }

            pageIndex = publishThemeData.getPageIndex();
        } else {
            pageIndex = publishThemeData.getPageIndex() + 1;
        }

        HttpManager.getInstance().getUserPublishedVideos(pageIndex, new Callback<AllUserThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                listener.onFailure(errorMsg);
            }

            @Override
            public void onSuccess(AllUserThemeBean allUserThemeBean) {
                if (allUserThemeBean != null && allUserThemeBean.getShow_list() != null && !allUserThemeBean.getShow_list().isEmpty()) {
                    publishThemeData.setPageIndex(allUserThemeBean.getPage_index());
                    ArrayList<Theme> dataList = Theme.transformData(allUserThemeBean);
                    if (isRefresh) {
                        publishThemeData.updateData(dataList);
                    } else {
                        publishThemeData.appendData(dataList);
                    }

                    listener.onSuccess(true);
                } else {
                    listener.onSuccess(false);
                }
            }
        });
    }

    public ArrayList<Theme> getUserPublishTheme() {
        if (publishThemeData == null) {
            return new ArrayList<>();
        }
        return publishThemeData.getDataList();
    }

    public void clearPublishData() {
        if (publishThemeData != null) {
            publishThemeData.clear();
        }
    }

    public ArrayList<Theme> getUserUploadTheme() {
        if (uploadThemeData == null) {
            return new ArrayList<>();
        }
        return uploadThemeData.getDataList();
    }

    public void clearUploadData() {
        if (uploadThemeData != null) {
            uploadThemeData.clear();
        }
    }

    private void loadRawThemesSync() {
        final ArrayList<Theme> oldThemes = new ArrayList<>(themes);
        synchronized (themes) {
            themes.clear();
            ArrayList<Type> types = Type.values();
            if (types.isEmpty() || !(types.get(0) instanceof Theme)) {
                ColorPhoneCrashlytics.getInstance().logException(new Exception("Theme load fail!"));
                return;
            }
            for (Type type : types) {
                if (!(type instanceof Theme)) {
                    continue;
                }

                if (type.getId() == Theme.RANDOM_THEME && !Ap.RandomTheme.enable()) {
                    HSLog.d("RandomTheme", "Unable");
                    continue;
                }
                if (type.getId() == Type.NONE) {
                    mThemeNone = (Theme) type;
                }
                themes.add((Theme) type);
            }
        }

        if (DEBUG_THEME_CHANGE) {
            mTestHandler.postDelayed(sTestRunnable, 8000);
        }
    }

    private static boolean isThemeChanged(ArrayList<Theme> themes, ArrayList<Theme> oldThemes) {
        if (themes.size() != oldThemes.size()) {
            return true;
        }
        final int size = themes.size();
        for (int i = 0; i < size; i++) {
            Theme t = themes.get(i);
            Theme t2 = oldThemes.get(i);
            if (t.getId() != t2.getId()
                    || !TextUtils.equals(t.getName(), t2.getName())
                    || !TextUtils.equals(t.getMp4Url(), t2.getMp4Url())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Theme> getThemesInner() {
        synchronized (themes) {
            if (themes.isEmpty()) {
                loadRawThemesSync();
            }
            if (mThemeNone != null) {
                themes.remove(mThemeNone);
            }
        }
        return themes;
    }

    public static ArrayList<Theme> themes() {
        return getInstance().getThemesInner();
    }

    /**
     * Base type of theme info has changed.
     * (Language change,or Remote config that define themes has changed)
     * <p>
     * Reload theme info from config file.
     */
    public void updateThemesTotally() {
        synchronized (themes) {
            //Type.updateTypes();

            themes.clear();
            loadRawThemesSync();
        }
    }

    public void fillData(ArrayList<Theme> mRecyclerViewData) {
        final List<Theme> bgThemes = updateThemes(false);
        // Data ready
        mRecyclerViewData.clear();
        mRecyclerViewData.addAll(bgThemes);
    }


    public void initThemes() {
        // First restore db values of download tasks.
        TasksManager.getImpl().init();

        // Load Raw theme. prepare theme list.
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                loadRawThemesSync();
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        updateThemes(true);
                    }
                });
            }
        });
    }

    @NonNull
    @DebugLog
    @MainThread
    public List<Theme> updateThemes(boolean onApplicationInit) {
        int selectedThemeId = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1);

        boolean isSpacialUser = RomUtils.checkIsMiuiRom() || RomUtils.checkIsVivoRom();
        boolean defaultTheme = HSConfig.optBoolean(false, "Application", "Theme", "DefaultTheme");
        boolean ddd = (selectedThemeId == -1) && (isSpacialUser ? defaultTheme : selectedThemeId == -1);
//        boolean applyDefaultTheme = (selectedThemeId == -1);

        boolean applyDefaultTheme;
        if (selectedThemeId == -1) {
            if (isSpacialUser) {
                applyDefaultTheme = defaultTheme;
            } else {
                applyDefaultTheme = true;
            }
        } else {
            applyDefaultTheme = false;
        }

        boolean autopilotRandomEnable = Ap.RandomTheme.enable();
        if (applyDefaultTheme) {
            selectedThemeId = autopilotRandomEnable ? Theme.RANDOM_THEME : Utils.getDefaultThemeId();
            HSLog.d("AP-ScreenFlash", "defaultThemeID : " + selectedThemeId);
            ThemePreviewView.saveThemeApplys(selectedThemeId);
            ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, selectedThemeId);
        }

        final List<Theme> bgThemes = new ArrayList<>(themes());

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

    @DebugLog
    private void updateThemeTasks(List<Theme> bgThemes, boolean applyDefaultTheme, int idDefault) {
        Theme needPreloadTheme = null;
        // Task update (if new theme added here, we update download task)
        for (Theme theme : bgThemes) {
            if (applyDefaultTheme && theme.getId() == idDefault) {
                needPreloadTheme = theme;
            }
        }

        // Prepare default theme
        if (needPreloadTheme != null
                && idDefault != Utils.localThemeId) {
            final Theme targetTheme = needPreloadTheme;
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    prepareThemeMediaFile(targetTheme);
                }
            });
        }

        // Prepare next random theme
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (Ap.RandomTheme.enable()) {
                    RandomTheme.getInstance().prepareNextTheme();
                }
            }
        });
    }

    private void prepareThemeMediaFile(Theme theme) {
        HSLog.d(TAG, "prepareDefaultThemeFile");
        TasksManager.getImpl().downloadTheme(theme, null);
    }

    private boolean isLikeTheme(String[] likeThemes, int themeId) {
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

    private String[] getThemeLikes() {
        String likes = HSPreferenceHelper.getDefault().getString(PREFS_THEME_LIKE, "");
        return likes.split(",");
    }

}
