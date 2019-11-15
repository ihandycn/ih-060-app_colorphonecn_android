package com.honeycomb.colorphone.wallpaper.task;


public class BackToDesktopCondition extends ObserverCondition {

    public BackToDesktopCondition() {
        setTag(TaskBlackboard.Conditions.BACK_TO_DESKTOP);
        setSufficient(false);
        setNecessary(true);
    }
}
