package com.honeycomb.colorphone.trigger;

import android.text.format.DateUtils;

import com.superapps.util.Preferences;

public abstract class NormalTrigger extends Trigger {

    private Preferences mPreferences;

    public NormalTrigger() {
        mPreferences = Preferences.get("NormalTrigger");
    }

    public abstract boolean enabled();

    @Override
    boolean onChance() {
        if (!enabled()) {
            return false;
        }

        Trigger parentTrigger = getParentTrigger();
        if (parentTrigger != null && !parentTrigger.onChance()) {
            return false;
        }

        Options options = getTriggerOptions();
        int count = mPreferences.getInt(getCountKeyName(), 0);
        long lasTimeTrigger = mPreferences.getLong(getTimeKeyName(), 0);

        boolean timeIntervalValid = options.intervalHours == 0
                || System.currentTimeMillis() - lasTimeTrigger <
                options.intervalHours * DateUtils.HOUR_IN_MILLIS;
        boolean countValid = options.totalLimitCount == 0
                || count < options.totalLimitCount;


        if (timeIntervalValid && countValid) {
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

    /**
     *
     */
    public void onConsumeChance(){
        mPreferences.incrementAndGetInt(getCountKeyName());
        mPreferences.putLong(getTimeKeyName(), System.currentTimeMillis());
        Trigger p = getParentTrigger();
        if (p != null) {
            p.onConsumeChance();
        }
    }

}
