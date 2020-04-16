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
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private Map<String, ThemeDataForCategory> categoryThemeDataMap = new HashMap<>();

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

    public void requestCategoryThemes(String categoryId, boolean isRefresh, ThemeUpdateListener listener) {
        int pageIndex;
        if (isRefresh) {
            if (categoryThemeDataMap.get(categoryId) == null) {
                ThemeDataForCategory themeDataForCategory = new ThemeDataForCategory();
                categoryThemeDataMap.put(categoryId, themeDataForCategory);
            } else {
                Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).clear();
            }

            pageIndex = 0;
        } else {
            pageIndex = Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).getPageIndex();
        }

        HttpManager.getInstance().getCategoryThemes(categoryId, pageIndex, new Callback<AllThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                listener.onFailure(errorMsg);
            }

            @Override
            public void onSuccess(AllThemeBean allThemeBean) {
                String HTTP_OK = "0000";
                String NO_MORE_DATA = "2000";
                if (HTTP_OK.equals(allThemeBean.getRetcode())) {
                    if (allThemeBean.getData() != null && !allThemeBean.getData().isEmpty()) {
                        Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).setPageIndex(Integer.valueOf(allThemeBean.getPx()));
                        if (isRefresh) {
                            Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).updateData(
                                    Theme.transformCategoryData(0, allThemeBean));
                        } else {
                            Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).appendData(
                                    Theme.transformCategoryData(Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).getThemeSize(), allThemeBean));
                        }

                        listener.onSuccess(true);
                    } else {
                        listener.onSuccess(false);
                    }
                } else if (NO_MORE_DATA.equals(allThemeBean.getRetcode())) {
                    listener.onSuccess(false);
                } else {
                    onFailure(allThemeBean.getRetdesc());
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

    public ArrayList<Theme> getCategoryThemes(String categoryId) {
        if (categoryThemeDataMap.get(categoryId) == null) {
            return new ArrayList<>();
        }
        List<Theme> bgThemes = updateThemes(Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).getDataList());
        return new ArrayList<>(bgThemes);
    }

    public void clearCategoryThemes(String categoryId) {
        if (categoryThemeDataMap.get(categoryId) != null) {
            Objects.requireNonNull(categoryThemeDataMap.get(categoryId)).clear();
        }
    }


    private void loadRawThemesSync() {
        synchronized (themes) {
            themes.clear();
            ArrayList<Type> types = Type.values();
            if (types.isEmpty() || !(types.get(0) instanceof Theme)) {
                return;
            }
            for (Type type : types) {
                if (!(type instanceof Theme)) {
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
        final List<Theme> bgThemes = updateThemes(themes());
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
                        updateThemes(themes());
                    }
                });
            }
        });
    }

    @NonNull
    @DebugLog
    @MainThread
    public List<Theme> updateThemes(ArrayList<Theme> themeList) {
        int selectedThemeId = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1);

        final List<Theme> bgThemes = new ArrayList<>(themeList);

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
        return bgThemes;
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
