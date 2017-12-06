package com.honeycomb.colorphone.contact;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.colorphone.lock.util.ConcurrentUtils;
import com.ihs.app.framework.HSApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by sundxing on 17/12/1.
 */

public class ContactManager {

    private static ContactManager INSTANCE;
    private final SQLiteDatabase mDb;
    private List<SimpleContact> mAllContacts = new ArrayList<>();
    private List<SimpleContact> mThemeFilterContacts = new ArrayList<>();
    private List<LoadCallback> mThemeContactsListener = new ArrayList<>();

    private boolean needFilterTheme = false;

    public ContactManager() {
        mDb = new ContactDBHelper(HSApplication.getContext()).getWritableDatabase();
    }

    public static void init() {
        INSTANCE = new ContactManager();
        INSTANCE.update();
    }

    public static ContactManager getInstance() {
        return INSTANCE;
    }

    public void update() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final List<SimpleContact> all = ContactUtils.readAllContacts(HSApplication.getContext());
                fillThemeContacts(all);
                mAllContacts.clear();
                mAllContacts.addAll(all);
                needFilterTheme = true;
            }
        });
    }

    public List<SimpleContact> getThemes(boolean onlyThemeSet) {
        if (onlyThemeSet) {
            if (needFilterTheme || mThemeFilterContacts.isEmpty()) {
                mThemeFilterContacts.clear();
                for (SimpleContact c : mAllContacts) {
                    if (c.getThemeId() != SimpleContact.INVALID_THEME) {
                        mThemeFilterContacts.add(c);
                    }
                }
                needFilterTheme = false;
            }
            return mThemeFilterContacts;
        } else {
            return mAllContacts;
        }
    }

    public void register(LoadCallback callback) {
        if (!mAllContacts.isEmpty()) {
            callback.onLoadFinish();
        } else {
            mThemeContactsListener.add(callback);
        }
    }

    public void unRegister(LoadCallback callback) {
        mThemeContactsListener.remove(callback);
    }

    @DebugLog
    private synchronized boolean fillThemeContacts(List<SimpleContact> allContacts) {
        SQLiteDatabase db = mDb;
        final Cursor c = db.rawQuery("SELECT * FROM " + ThemeEntry.TABLE_NAME, null);

        final List<SimpleContact> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return false;
            }

            do {
                String rawNumber = c.getString(c.getColumnIndex(ThemeEntry.NUMBER));

                SimpleContact model = findContact(rawNumber, allContacts);
                if (model != null) {
//                    model.setName(c.getString(c.getColumnIndex(ThemeEntry.NAME)));
                    model.setRawNumber(rawNumber);
//                    model.setPhotoUri(c.getString(c.getColumnIndex(ThemeEntry.PHOTO_URI)));
                    model.setThemeId(c.getInt(c.getColumnIndex(ThemeEntry.THEME_ID)));
                    Log.d("Read theme contact", model.toString());
                    list.add(model);
                }
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return true;
    }

    private SimpleContact findContact(String rawNumber, List<SimpleContact> allContacts) {
        for (SimpleContact contact : allContacts) {
            if (PhoneNumberUtils.compare(contact.getRawNumber(), rawNumber)) {
                return contact;
            }
        }
        return null;
    }

    public void updateDb(final List<ThemeEntry> themes, Runnable callback){
        final WeakReference<Runnable> weakReference = new WeakReference<Runnable>(callback);
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                ContentValues initialValues = new ContentValues();
                final SQLiteDatabase database = mDb;
                long result = 0;
                try {
                    database.beginTransaction();

                    for (ThemeEntry entry : themes) {
                        entry.toContentValues(initialValues);
                        if (entry.mAction == ContactDBHelper.Action.UPDATE) {
                            result = database.update(ThemeEntry.TABLE_NAME, initialValues,
                                    ThemeEntry.NUMBER + " = ?", new String[]{entry.getRawNumber()});
                        } else if (entry.mAction == ContactDBHelper.Action.INSERT) {
                            result = database.insert(ThemeEntry.TABLE_NAME, null, initialValues);
                        } else if (entry.mAction == ContactDBHelper.Action.DELETE) {
                            result = database.delete(ThemeEntry.TABLE_NAME, ThemeEntry.NUMBER + " = ?", new String[]{entry.getRawNumber()});
                        }
                        Log.d("Update theme contact", entry.toString());

                    }
                    database.setTransactionSuccessful();

                    ConcurrentUtils.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Runnable callback = weakReference.get();
                            if (callback != null) {
                                callback.run();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                } finally{
                    database.endTransaction();
                }

            }
        });
    }

    public void markDataChanged() {
        needFilterTheme = true;
    }

    public interface LoadCallback {
        void onLoadFinish();
    }
}
