package com.acb.libwallpaper.live.task;

public class ObserverCondition extends Condition {

    private boolean mMeet;

    ObserverCondition() {
        super();
    }

    public ObserverCondition(String tag) {
        super(tag);
    }

    public ObserverCondition(String tag, boolean sufficient, boolean necessary) {
        super(tag, sufficient, necessary);
    }

    @Override
    public boolean meet() {
        return mMeet;
    }

    void notify(boolean meet) {
        mMeet = meet;
        for (String taskKey : mAffinityTask) {
            if (meet) {
                ConditionManager.getInstance().notifyTask(taskKey);
            }
        }
    }
}
