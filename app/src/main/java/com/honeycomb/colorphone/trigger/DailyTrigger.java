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
    public Options getTriggerOptions() {
        return new Options(0, 0, 1);
    }

    @Override
    public String getName() {
        return "DailyTrigger" + mExtraName;
    }

    @Override
    public Trigger getParentTrigger() {
        return null;
    }
}
