package com.honeycomb.colorphone;

import com.ihs.app.framework.HSApplication;

public interface AppInit {
    void onInit(HSApplication application);
    boolean onlyInMainProcess();

    /**
     * Delay init option
     * @return  if true, init delay until full displayed .
     */
     boolean afterAppFullyDisplay();
}
