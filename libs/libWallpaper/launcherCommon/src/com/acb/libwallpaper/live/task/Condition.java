package com.acb.libwallpaper.live.task;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Condition {

    String mTag;
    private boolean mSufficient;
    private boolean mNecessary;
    List<String> mAffinityTask = new ArrayList<>();

    Condition(){}

    Condition(String tag) {
        this(tag, false, false);
    }

    Condition(String tag, boolean sufficient, boolean necessary) {
        mTag = tag;
        mSufficient = sufficient;
        mNecessary = necessary;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setSufficient(boolean sufficient) {
        mSufficient = sufficient;
    }

    public void setNecessary(boolean necessary) {
        mNecessary = necessary;
    }

    public abstract boolean meet();

    boolean isSufficient() {
        return mSufficient;
    }

    boolean isNecessary() {
        return mNecessary;
    }

    public String getTag() {
        return mTag;
    }

    void attachToTask(Task task) {
        mAffinityTask.add(task.getTag());
    }

    void detachFromTask(Task task) {
        mAffinityTask.remove(task.getTag());
    }

    boolean hasAffinityTask() {
        return mAffinityTask.size() == 0;
    }

    @Override
    public String toString() {
        return mTag + " isSufficient: " + mSufficient + " isNecessary: " + mNecessary;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Condition && TextUtils.equals(((Condition) obj).mTag, mTag)
                && mSufficient == ((Condition) obj).isSufficient()
                && mNecessary == ((Condition) obj).isNecessary();
    }
}
