package com.honeycomb.colorphone.triviatip;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.honeycomb.colorphone.Constants;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TriviaDataManager implements TriviaTip.OnTipShowListener, INotificationObserver {

    private static final String PREF_KEY_LAST_SHOW_ITEM_ID = "last_show_item_id";

    private SparseArray<TriviaItem> mItems = new SparseArray<>();
    private int mMaxId;
    private static TriviaDataManager INSTANCE;

    public static TriviaDataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TriviaDataManager();
        }
        return INSTANCE;
    }

    TriviaDataManager() {
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, this);
        Map<String, ?> configData = HSConfig.getMap("Application", "TriviaFact");
        parseConfig(configData);
    }

    private void parseConfig(Map<String, ?> config) {
        if (config == null) {
            return;
        }

        List<TriviaItem> triviaItems = TriviaItem.fromConfig(config);

        HSLog.d("TriviaTip", "parse size: " + triviaItems.size());

        // Publish
        synchronized (this) {
            mItems.clear();
            mMaxId = 0;
            for (TriviaItem triviaItem : triviaItems) {
                mItems.put(triviaItem.id, triviaItem);
                if (triviaItem.id > mMaxId) {
                    mMaxId = triviaItem.id;
                }
            }
        }
    }

    public List<TriviaItem> getItems(int size) {
        ArrayList<TriviaItem> triviaItems = new ArrayList<TriviaItem>(size);
        int nextShowId = 0;
        TriviaItem triviaItem = null;
        while (nextShowId <= mMaxId) {
            triviaItem = mItems.get(nextShowId);
            if (triviaItem != null) {
                triviaItems.add(triviaItem);
            }
            if (triviaItems.size() >= size) {
                break;
            }
            nextShowId++;
        }
        return triviaItems;
    }

    @Nullable
    TriviaItem getCurrentItem() {
        if (mItems.size() == 0) {
            return null;
        }
        int lastShowId = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_LAST_SHOW_ITEM_ID, -1);
        int nextShowId = lastShowId + 1;
        TriviaItem triviaItem = null;
        while (nextShowId <= mMaxId) {
            triviaItem = mItems.get(nextShowId);
            if (triviaItem != null) {
                break;
            }
            nextShowId++;
        }
        return triviaItem;
    }

    @Override
    public void onShow(TriviaItem item) {
        Preferences.get(Constants.DESKTOP_PREFS).putInt(PREF_KEY_LAST_SHOW_ITEM_ID, item.id);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        HSLog.d("TriviaTip", "Config change");
        Map<String, ?> configData = HSConfig.getMap("Application", "TriviaFact");
        parseConfig(configData);
    }
}
