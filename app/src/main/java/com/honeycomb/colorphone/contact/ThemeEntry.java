package com.honeycomb.colorphone.contact;

import android.content.ContentValues;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

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

    public static List<ThemeEntry> valueOf(SimpleContact contact, ContactDBHelper.Action action) {
        List<ThemeEntry> entries = new ArrayList<>();
        ThemeEntry entry = new ThemeEntry();
        entry.setContactId(contact.getContactId());
        entry.setThemeId(contact.getThemeId());
        entry.setPhotoUri(contact.getPhotoUri());
        entry.setRawNumber(contact.getRawNumber());
        entry.setSelected(contact.isSelected());
        entry.setName(contact.getName());
        entry.mAction = action;
        entries.add(entry);

        if (contact.getOtherNumbers() != null) {
            for (String number : contact.getOtherNumbers()) {
                ThemeEntry entryOther = entry.clone();
                entryOther.setRawNumber(number);
                entries.add(entryOther);
            }
        }
        return entries;
    }

    public ThemeEntry clone() {
        ThemeEntry entry = new ThemeEntry();
        entry.setContactId(this.getContactId());
        entry.setThemeId(this.getThemeId());
        entry.setPhotoUri(this.getPhotoUri());
        entry.setRawNumber(this.getRawNumber());
        entry.setSelected(this.isSelected());
        entry.setName(this.getName());
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
