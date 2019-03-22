package com.honeycomb.colorphone.trigger;

public abstract class Trigger {
    public abstract Options getTriggerOptions();
    public abstract String getName();

    public void onConsumeChance(){
    }
    public abstract Trigger getParentTrigger();
    /**
     *
     * @return true if triggered
     * @param result size 3
     *               result[0] intervalMills is ok;
     *               result[1] totalLimitCount is ok;
     *               result[2] dailyLimitCount is ok;
     */
    abstract boolean onChance(boolean[] result);

    public static class Options {
        public long intervalMills;
        public int totalLimitCount;
        public int dailyLimitCount;

        public Options() {
        }

        public Options(long intervalMills, int totalLimitCount, int dailyLimitCount) {
            this.intervalMills = intervalMills;
            this.totalLimitCount = totalLimitCount;
            this.dailyLimitCount = dailyLimitCount;
        }
    }

}
