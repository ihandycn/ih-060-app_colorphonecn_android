package com.honeycomb.colorphone.trigger;

import com.honeycomb.colorphone.cashcenter.CashUtils;

public class CashCenterTriggerList {

    private static CashCenterTriggerList INSTANCE = new CashCenterTriggerList();

    private CashCenterTriggerList() {
    }

    public static CashCenterTriggerList getInstance() {
        return INSTANCE;
    }


    private NormalTrigger onceEntranceTrigger = new NormalTrigger() {

        @Override
        public boolean enabled() {
            // Autopilot & config
            return !CashUtils.hasUserEnterCrashCenter();
        }

        @Override
        Options getTriggerOptions() {
            Trigger.Options options = new Trigger.Options();
            options.intervalHours = 0; // No time limit
            options.dailyLimitCount = 3; // Consume change only 3 times a day.
            return options;
        }

        @Override
        String getName() {
            return "OnceEntranceCashCenter";
        }

        @Override
        Trigger getParentTrigger() {
            return null;
        }
    };

    private NormalTrigger mBackToMainTrigger = new NormalTrigger() {

        @Override
        public boolean enabled() {
            // Autopilot & config
            return CashUtils.guideShowOnBacktoMain();
        }

        @Override
        Options getTriggerOptions() {
            Trigger.Options options = new Trigger.Options();
            options.intervalHours = 2;
            options.totalLimitCount = 3;
            return options;
        }

        @Override
        String getName() {
            return "BackToMain";
        }

        @Override
        Trigger getParentTrigger() {
            return onceEntranceTrigger;
        }
    };

    private NormalTrigger mUnlockScreenTrigger = new NormalTrigger() {

        @Override
        public boolean enabled() {
            // Autopilot & config
            return CashUtils.guideShowOnBacktoMain();
        }

        @Override
        Options getTriggerOptions() {
            Trigger.Options options = new Trigger.Options();
            options.intervalHours = 2;
            options.totalLimitCount = 3;
            return options;
        }

        @Override
        String getName() {
            return "UnlockScreen";
        }

        @Override
        Trigger getParentTrigger() {
            return onceEntranceTrigger;
        }
    };

    private NormalTrigger mCallAlertClose = new NormalTrigger() {

        @Override
        public boolean enabled() {
            // Autopilot & config
            return CashUtils.guideShowOnBacktoMain();
        }

        @Override
        Options getTriggerOptions() {
            Trigger.Options options = new Trigger.Options();
            options.intervalHours = 2;
            options.totalLimitCount = 3;
            return options;
        }

        @Override
        String getName() {
            return "CallAlertClose";
        }

        @Override
        Trigger getParentTrigger() {
            return onceEntranceTrigger;
        }
    };

    public boolean checkAt(CashUtils.Source source, boolean autoConsume) {
        Trigger trigger = getTriggerBySource(source);
        if (trigger != null) {
            boolean isTrigger = trigger.onChance();
            if (isTrigger && autoConsume) {
                trigger.onConsumeChance();
            }
            return isTrigger;
        }
        return false;
    }

    public void consumeChance(CashUtils.Source source) {
        Trigger trigger = getTriggerBySource(source);
        if (trigger != null) {
            trigger.onConsumeChance();
        }
    }

    private Trigger getTriggerBySource(CashUtils.Source source) {
        Trigger trigger = null;
        switch (source) {
            case Inner:
                trigger = mBackToMainTrigger;
                break;
            case UnlockScreen:
                trigger = mUnlockScreenTrigger;
                break;
            case CallAlertClose:
                trigger = mCallAlertClose;
                break;
        }
        return trigger;
    }


}
