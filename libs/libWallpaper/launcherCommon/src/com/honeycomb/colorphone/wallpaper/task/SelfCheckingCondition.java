package com.honeycomb.colorphone.wallpaper.task;


public abstract class SelfCheckingCondition extends Condition {

    public SelfCheckingCondition(String tag) {
        super(tag);
    }

    public SelfCheckingCondition(String tag, boolean sufficient, boolean necessary) {
        super(tag, sufficient, necessary);
    }
}
