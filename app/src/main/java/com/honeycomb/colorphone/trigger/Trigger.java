package com.honeycomb.colorphone.trigger;

public abstract class Trigger {
    abstract Options getTriggerOptions();
    abstract String getName();

    public void onConsumeChance(){
    }
    abstract Trigger getParentTrigger();
    /**
     *
     * @return true if triggered
     */
    abstract boolean onChance();

    public static class Options {
        public int intervalHours;
        public int totalLimitCount;

        //TODO
        public int dailyLimitCount;
    }

}
