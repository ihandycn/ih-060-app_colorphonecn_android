package com.honeycomb.colorphone.contact;

import android.content.ContentValues;
import android.provider.BaseColumns;

/**
 * Created by sundxing on 17/12/1.
 */
public class ThemeEntry extends SimpleContact implements BaseColumns {

    public static final String TABLE_NAME = "theme";
    public static final String NAME = "name";
    public static final String NUMBER = "number";
    public static final String PHOTO_URI = "photo_uri";
    public static final String THEME_ID = "theme_id";

    public ContactDBHelper.Action mAction;

    public static ThemeEntry valueOf(SimpleContact contact) {
        ThemeEntry entry = new ThemeEntry();
        entry.setThemeId(contact.getThemeId());
        entry.setPhotoUri(contact.getPhotoUri());
        entry.setRawNumber(contact.getRawNumber());
        entry.setSelected(contact.isSelected());
        entry.setName(contact.getName());
        return entry;
    }

    public ContentValues toContentValues(ContentValues values) {
        ContentValues cv = values == null ? new ContentValues() : values;
        cv.put(NAME, getName());
        cv.put(NUMBER, getRawNumber());
        cv.put(PHOTO_URI, getPhotoUri());
        cv.put(THEME_ID, getThemeId());
        return cv;
    }

    @Override
    public String toString() {
        return super.toString() + ",Action = " + mAction;
    }
}
