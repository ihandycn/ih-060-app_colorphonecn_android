package com.honeycomb.colorphone.contact;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.colorphone.lock.util.ConcurrentUtils;
import com.ihs.app.framework.HSApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/12/1.
 */

public class ContactManager {

    private static ContactManager INSTANCE;
    private final SQLiteDatabase mDb;
    private List<SimpleContact> mLocalContacts;
    private List<SimpleContact> mThemeContacts;

    private List<LoadCallback> mLocalContactsListener = new ArrayList<>();
    private List<LoadCallback> mThemeContactsListener = new ArrayList<>();

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
                mLocalContacts = ContactUtils.readAllContacts(HSApplication.getContext());
                mThemeContacts = getAllThemeContacts();
            }
        });
    }

    public void getLocalContacts(LoadCallback callback) {
        if (mLocalContacts != null) {
            callback.onLoadFinish(new ArrayList<SimpleContact>(mLocalContacts));
        } else {
            mLocalContactsListener.add(callback);
        }
    }

    public void getThemeContacts(LoadCallback callback) {
        if (mThemeContacts != null) {
            callback.onLoadFinish(new ArrayList<SimpleContact>(mThemeContacts));
        } else {
            mThemeContactsListener.add(callback);
        }
    }

    private List<SimpleContact> getAllThemeContacts() {
        SQLiteDatabase db = mDb;
        final Cursor c = db.rawQuery("SELECT * FROM " + ThemeEntry.TABLE_NAME, null);

        final List<SimpleContact> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }

            do {
                SimpleContact model = new SimpleContact();
                model.setName(c.getString(c.getColumnIndex(ThemeEntry.NAME)));
                model.setRawNumber(c.getString(c.getColumnIndex(ThemeEntry.NUMBER)));
                model.setPhotoUri(c.getString(c.getColumnIndex(ThemeEntry.PHOTO_URI)));
                model.setThemeId(c.getInt(c.getColumnIndex(ThemeEntry.THEME_ID)));
                Log.d("Read theme contact", model.toString());
                list.add(model);
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return list;
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

    public boolean themeSetAlready(SimpleContact c) {
        return mThemeContacts.contains(c);
    }

    public interface LoadCallback {
        void onLoadFinish(List<SimpleContact> contacts);
    }
}
