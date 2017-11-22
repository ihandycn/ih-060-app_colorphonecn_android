package com.honeycomb.colorphone.notification;

import com.acb.call.themes.Type;

import java.util.ArrayList;

/**
 * Created by ihandysoft on 2017/11/21.
 */

public class NotificationManager {
    private static final NotificationManager ourInstance = new NotificationManager();

    public static NotificationManager getInstance() {
        return ourInstance;
    }

    private NotificationManager() {

    }

    public void setNewThemeList(ArrayList<Type> types) {

    }
}
