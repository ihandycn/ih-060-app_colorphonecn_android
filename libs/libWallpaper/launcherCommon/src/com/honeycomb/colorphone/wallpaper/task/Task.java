package com.honeycomb.colorphone.wallpaper.task;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Task {

    private static final String TAG = ConditionManager.class.getSimpleName();

    private Runnable mRunnable;
    private List<Condition> mConditions = new ArrayList<>(3);
    private String mTag;

    Task(String tag) {
        mTag = tag;
    }

    void execute() {
        if (mRunnable != null) {
            mRunnable.run();
        }
    }

    boolean ready() {
        boolean notMeetAllNecessaryCondition = false;
        for (Condition condition : mConditions) {
            if (condition.isSufficient() && condition.meet()) {
                HSLog.d(TAG, "Meet sufficient condition: " + condition);
                return true;
            }
            if (condition.isNecessary() && !condition.meet()) {
                HSLog.d(TAG, "Not meet necessary condition: " + condition);
                notMeetAllNecessaryCondition = true;
            }

        }
        return !notMeetAllNecessaryCondition;
    }

    public void setRunnable(Runnable r) {
        mRunnable = r;
    }

    public void addCondition(Condition condition) {
        Map<String, ObserverCondition> observerCondition = ConditionManager.getInstance().getObserverCondition();
        ObserverCondition existCondition = observerCondition.get(condition.getTag());
        if (existCondition != null) {
            condition = existCondition;
        } else if (condition instanceof ObserverCondition) {
            ConditionManager.getInstance().addObserverCondition((ObserverCondition) condition);
        }
        condition.attachToTask(this);
        mConditions.add(condition);
    }

    public void removeCondition(Condition condition) {
        mConditions.remove(condition);
    }

    public String getTag() {
        return mTag;
    }

    List<Condition> getConditions() {
        return mConditions;
    }

    @Override
    public String toString() {
        return "Task: " + mTag;
    }
}
