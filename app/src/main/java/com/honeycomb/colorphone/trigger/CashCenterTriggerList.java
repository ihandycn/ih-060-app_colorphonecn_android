package com.honeycomb.colorphone.trigger;

public class CashCenterTriggerList {


    private NormalTrigger mBackToMainGuide = new NormalTrigger() {

        @Override
        public boolean enabled() {
            // Autopilot & config
            return false;
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
            return "BackToMainGuide";
        }

        @Override
        Trigger getParentTrigger() {
            return null;
        }
    };
}
