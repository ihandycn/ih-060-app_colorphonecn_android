package com.acb.libwallpaper.live.task;

import com.acb.libwallpaper.live.model.LauncherFiles;
import com.superapps.util.Preferences;

public class CountableObserverCondition extends ObserverCondition {

    private int mMeetCount;
    private String mPrefKey;
    private String mObserverTag;

    public CountableObserverCondition(String observerTag, int meetCount) {
        mMeetCount = meetCount;
        mPrefKey = observerTag + "_" + meetCount;
        mObserverTag = observerTag;
        setTag(mPrefKey);
    }

    String getObserverTag() {
        return mObserverTag;
    }

    @Override
    void notify(boolean meet) {
        int count = 0;
        if (meet) {
            count = Preferences.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(mPrefKey);
        }
        if (count != mMeetCount) {
            meet = false;
        }
        super.notify(meet);
    }
}
