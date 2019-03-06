package com.honeycomb.colorphone.trigger;

public class DailyTrigger extends NormalTrigger {
    private String mExtraName = "";
    public DailyTrigger() {}
    public DailyTrigger(String name) {
        mExtraName = name;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    Options getTriggerOptions() {
        return new Options(0, 0, 1);
    }

    @Override
    String getName() {
        return "DailyTrigger" + mExtraName;
    }

    @Override
    Trigger getParentTrigger() {
        return null;
    }
}
