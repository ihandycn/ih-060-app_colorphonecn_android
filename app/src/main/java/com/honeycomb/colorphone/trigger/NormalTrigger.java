package com.honeycomb.colorphone.trigger;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;

public abstract class NormalTrigger extends Trigger {

    protected Preferences mPreferences;

    public NormalTrigger() {
        mPreferences = Preferences.get("NormalTrigger");
    }

    public abstract boolean enabled();

    @Override
    public boolean onChance(boolean[] result) {
        if (!enabled()) {
            return false;
        }

        Trigger parentTrigger = getParentTrigger();
        if (parentTrigger != null && !parentTrigger.onChance(null)) {
            return false;
        }

        Options options = getTriggerOptions();
        int count = mPreferences.getInt(getCountKeyName(), 0);
        long lasTimeTrigger = mPreferences.getLong(getTimeKeyName(), 0);

        boolean timeIntervalValid = options.intervalMills == 0
                || System.currentTimeMillis() - lasTimeTrigger >
                options.intervalMills;
        boolean countValid = options.totalLimitCount == 0
                || count < options.totalLimitCount;


        boolean dailyCountValid = true;
        int dailyLimit = options.dailyLimitCount;
        if (dailyLimit > 0) {
            int todayCount = mPreferences.getInt(getDailyCountKeyName(), 0);
            boolean sameDay = Calendars.isSameDay(System.currentTimeMillis(), lasTimeTrigger);
            if (sameDay) {
                dailyCountValid = todayCount < dailyLimit;
            } else {
                // reset
                if (todayCount > 0) {
                    mPreferences.putInt(getDailyCountKeyName(), 0);
                }
                dailyCountValid = true;
            }
        }

        if (result != null && result.length >= 3) {
            result[0] = timeIntervalValid;
            result[1] = countValid;
            result[2] = dailyCountValid;
        }

        HSLog.d("NormalTrigger-" + getName(), "timeIntervalValid = " + timeIntervalValid
        +", countValid = " + countValid
        +", dailyCountValid = " + dailyCountValid);

        if (timeIntervalValid && countValid && dailyCountValid) {
            return true;
        }
        return false;
    }

    private String getTimeKeyName() {
        return getName() + "_lastTriggerTime";
    }

    private String getCountKeyName() {
        return getName() + "_count";
    }

    private String getDailyCountKeyName() {
        return getName() + "_count_daily";
    }

    public int currentTriggerCount() {
        return mPreferences.getInt(getCountKeyName(), 0);
    }

    /**
     * Call when use this chance to do something.
     */
    @Override
    public void onConsumeChance(){
        HSLog.d("NormalTrigger-" + getName(), "onConsumeChange");
        mPreferences.incrementAndGetInt(getCountKeyName());
        mPreferences.incrementAndGetInt(getDailyCountKeyName());
        mPreferences.putLong(getTimeKeyName(), System.currentTimeMillis());
        Trigger p = getParentTrigger();
        if (p != null) {
            p.onConsumeChance();
        }
    }

}
