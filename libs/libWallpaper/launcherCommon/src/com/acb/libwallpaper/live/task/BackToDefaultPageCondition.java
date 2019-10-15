package com.acb.libwallpaper.live.task;

public class BackToDefaultPageCondition extends ObserverCondition {

    public BackToDefaultPageCondition() {
        setTag(TaskBlackboard.Conditions.BACK_TO_DEFAULT_PAGE);
        setSufficient(false);
        setNecessary(true);
    }
}
