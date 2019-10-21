package com.acb.libwallpaper.live.task;

import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.acb.libwallpaper.live.debug.Logger;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConditionManager {

    private static final String TAG = ConditionManager.class.getSimpleName();

    private static ConditionManager sInstance;

    private Map<String, Task> mTasks = new HashMap<>();
    private Map<String, ObserverCondition> mObserverConditions = new HashMap<>();

    private ConditionManager() {
    }

    public static ConditionManager getInstance() {
        if (sInstance == null) {
            sInstance = new ConditionManager();
        }
        return sInstance;
    }

    /**
     * Call {@link #releaseTask(String)} when not used.
     */
    public Task acquireTask(String key) {
        assertOnMainThread();
        Task task = mTasks.get(key);
        if (task == null) {
            task = new Task(key);
            mTasks.put(key, task);
        }
        return task;
    }

    public @Nullable
    Task releaseTask(String key) {
        assertOnMainThread();
        Task removed = mTasks.remove(key);
        if (removed != null) {
            List<Condition> conditions = removed.getConditions();
            Iterator<Condition> iterator = conditions.iterator();
            while (iterator.hasNext()) {
                Condition next = iterator.next();
                iterator.remove();
                next.detachFromTask(removed);
                if (!next.hasAffinityTask()) {
                    mObserverConditions.remove(next.getTag());
                }
            }
        }
        return removed;
    }

    public void notifyCondition(String tag, boolean meet) {
        assertOnMainThread();
        List<ObserverCondition> observerConditions = new ArrayList<>();
        ObserverCondition observerCondition = mObserverConditions.get(tag);
        if (observerCondition != null) {
            observerConditions.add(observerCondition);
        }
        for (ObserverCondition condition : mObserverConditions.values()) {
            if (condition instanceof CountableObserverCondition && TextUtils.equals(tag, ((CountableObserverCondition)
                    condition).getObserverTag())) {
                observerConditions.add(condition);
            }
        }
        if (observerConditions.size() == 0) {
            Logger.log(TAG, "Condition " + tag + " does not exist");
        } else {
            for (ObserverCondition condition : observerConditions) {
                Logger.log(TAG, "Notify condition " + condition.getTag() + " meet: " + meet);
                condition.notify(meet);
            }
        }
    }

    void notifyTask(String key) {
        assertOnMainThread();
        Task task = mTasks.get(key);
        if (task == null) {
            HSLog.d(TAG, "Task " + key + " does not exist");
        } else if (task.ready()) {
            task.execute();
            HSLog.d(TAG, "Execute task " + key);
        } else {
            HSLog.d(TAG, "Task " + key + " is not ready");
        }
    }

    Map<String, ObserverCondition> getObserverCondition() {
        return mObserverConditions;
    }

    void addObserverCondition(ObserverCondition condition) {
        assertOnMainThread();
        mObserverConditions.put(condition.getTag(), condition);
    }

    private void assertOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ConditionManager must be used on main thread.");
        }
    }
}
