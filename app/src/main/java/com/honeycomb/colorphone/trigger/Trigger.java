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
        public int dailyLimitCount;

        public Options() {
        }

        public Options(int intervalHours, int totalLimitCount, int dailyLimitCount) {
            this.intervalHours = intervalHours;
            this.totalLimitCount = totalLimitCount;
            this.dailyLimitCount = dailyLimitCount;
        }
    }

}
