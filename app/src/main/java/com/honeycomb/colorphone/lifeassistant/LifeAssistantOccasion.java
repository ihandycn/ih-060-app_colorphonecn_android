package com.honeycomb.colorphone.lifeassistant;

import com.ihs.app.framework.HSApplication;
import com.superapps.occasion.Occasion;
import com.superapps.occasion.OccasionPriority;
import com.superapps.util.Navigations;

public class LifeAssistantOccasion implements Occasion {
    @Override public boolean show() {
        Navigations.startActivitySafely(HSApplication.getContext(), LifeAssistantActivity.class);
        return true;
    }

    @Override public boolean isValid() {
        return LifeAssistantConfig.canShowLifeAssistant();
    }

    @Override public int getPriority() {
        return OccasionPriority.SCREEN_GREETING.getPriority();
    }
}
