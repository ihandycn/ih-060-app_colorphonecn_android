package com.honeycomb.colorphone.trigger;

public class DailyTrigger extends NormalTrigger {
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
        return "DailyTrigger";
    }

    @Override
    Trigger getParentTrigger() {
        return null;
    }
}
