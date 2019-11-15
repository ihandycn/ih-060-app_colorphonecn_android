package com.honeycomb.colorphone.contact;

import android.Manifest;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.honeycomb.colorphone.contact.ContactDBHelper.Action.INSERT;

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
        boolean success = INSTANCE.update();
        if (!success) {
            HSGlobalNotificationCenter.addObserver(RuntimePermissions.NOTIFY_PERMISSION_GRANTED, new INotificationObserver() {
                @Override
                public void onReceive(String s, HSBundle hsBundle) {
                    String permName = hsBundle.getString(RuntimePermissions.NOTIFY_EXTRA_PERMISSION_NAME);
                    if (Manifest.permission.READ_CONTACTS.equals(permName)) {
                        INSTANCE.update();
                    }
                }
            });
        }
    }

    public static ContactManager getInstance() {
        return INSTANCE;
    }

    public boolean update() {
        boolean hasContactPerm = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_CONTACTS)
                == RuntimePermissions.PERMISSION_GRANTED;
        if (!hasContactPerm) {
            return false;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final List<SimpleContact> all = ContactUtils.readAllContacts(HSApplication.getContext());
                fillThemeContacts(all);
                synchronized (ContactManager.this) {
                    mAllContacts.clear();
                    mAllContacts.addAll(all);
                }
                needFilterTheme = true;
            }
        });
        return true;
    }

    public List<SimpleContact> getThemes(boolean onlyThemeSet) {
        if (onlyThemeSet) {
            updateFilterContactsIfNeeded();
            return mThemeFilterContacts;
        } else {
            return mAllContacts;
        }
    }

    private synchronized void updateFilterContactsIfNeeded() {
        if (needFilterTheme || mThemeFilterContacts.isEmpty()) {
            mThemeFilterContacts.clear();
            synchronized(this) {
                for (SimpleContact c : mAllContacts) {
                    if (c.getThemeId() != SimpleContact.INVALID_THEME) {
                        mThemeFilterContacts.add(c);
                    }
                }
            }
            needFilterTheme = false;
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

    /**
     * Fill contact with theme id.
     * Contact infos are from local contact database(keep data always new).
     * @param allContacts
     * @return
     */
    @DebugLog
    private synchronized boolean fillThemeContacts(List<SimpleContact> allContacts) {
        SQLiteDatabase db = mDb;

        // Hisense F23, table not create.
        Cursor c = null;

        try {
            c = db.rawQuery("SELECT * FROM " + ThemeEntry.TABLE_NAME, null);
            if (!c.moveToLast()) {
                return false;
            }

            if (BuildConfig.DEBUG) {
                int count = c.getCount();
                HSLog.d("Read theme contact count : " + count);
            }

            do {
                String rawNumber = c.getString(c.getColumnIndex(ThemeEntry.NUMBER));
                SimpleContact model = findContact(rawNumber, allContacts);
                if (model != null) {
                    model.setThemeId(c.getInt(c.getColumnIndex(ThemeEntry.THEME_ID)));
                    Log.d("Read theme contact", model.toString());
                }
            } while (c.moveToPrevious());
        } catch (SQLiteException e) {
            // ignore
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return true;
    }


    /**
     * Fetch theme config from our own database.
     * @return
     */
    @DebugLog
    private synchronized List<SimpleContact> fetchThemeContacts() {
        SQLiteDatabase db = mDb;
        final Cursor c = db.rawQuery("SELECT * FROM " + ThemeEntry.TABLE_NAME, null);

        final List<SimpleContact> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }

            do {
                String rawNumber = c.getString(c.getColumnIndex(ThemeEntry.NUMBER));

                SimpleContact model = new SimpleContact();
                model.setName(c.getString(c.getColumnIndex(ThemeEntry.NAME)));
                model.setRawNumber(rawNumber);
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

    private SimpleContact findContact(String rawNumber, List<SimpleContact> allContacts) {
        for (SimpleContact contact : allContacts) {
            if (contact.getThemeId() > 0) {
                continue;
            }
            if (isMatchPhoneNumber(contact, rawNumber)) {
                return contact;
            }

        }
        return null;
    }

    public boolean isMatchPhoneNumber(SimpleContact contact, String rawNumber) {
        if (PhoneNumberUtils.compare(contact.getRawNumber(), rawNumber)) {
            return true;
        } else if (contact.getOtherNumbers() != null) {
            // Other phone numbers
            for (String otherNumber : contact.getOtherNumbers()) {
                if (PhoneNumberUtils.compare(otherNumber, rawNumber)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateDb(final List<ThemeEntry> themes, Runnable callback){
        final WeakReference<Runnable> weakReference = new WeakReference<Runnable>(callback);
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                ContentValues initialValues = new ContentValues();
                final SQLiteDatabase database = mDb;
                long result = 0;
                try {
                    database.beginTransaction();

                    for (ThemeEntry entry : themes) {
                        markDataChanged();
                        entry.toContentValues(initialValues);
                        if (entry.mAction == ContactDBHelper.Action.UPDATE) {
                            result = database.update(ThemeEntry.TABLE_NAME, initialValues,
                                    ThemeEntry.NUMBER + " = ?", new String[]{entry.getRawNumber()});
                        } else if (entry.mAction == INSERT) {
                            result = database.insert(ThemeEntry.TABLE_NAME, null, initialValues);
                        } else if (entry.mAction == ContactDBHelper.Action.DELETE) {
                            result = database.delete(ThemeEntry.TABLE_NAME, ThemeEntry.NUMBER + " = ?", new String[]{entry.getRawNumber()});
                        }
                        Log.d("Update theme contact", entry.toString());
                        // Ringtone set

                        //todo null pointer Exception which result in database write failed;
//                        if (entry.mAction != null) {
//                            setContactRingtone(entry, entry.mAction != DELETE);
//                        }
                    }
                    database.setTransactionSuccessful();

                    Threads.postOnMainThread(new Runnable() {
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

    /**
     * Toggle ringtone switch, we update all contacts used this theme. <br>
     * Call this on work thread !
     * @param theme target theme
     * @param select theme select or not
     */
    public void updateRingtoneOnTheme(Theme theme, boolean select) {
        checkThread();
        final int themeId = theme.getId();
        final List<SimpleContact> contacts = getThemes(true);
        for (SimpleContact contact : contacts) {
            if (contact.getThemeId() == themeId) {
                setContactRingtone(contact, select);
                if (BuildConfig.DEBUG) {
                    HSLog.d("Ringtone", "Contact: " + contact.getName() + " is select = " + select);
                }
            }
        }
    }

    private void setContactRingtone(SimpleContact entry, boolean select) {
        checkThread();
        try {
            if (select) {
                // TODO thread safe ?
                Theme theme = ThemeApplyManager.getInstance().getAppliedThemeByThemeId(entry.getThemeId());
                RingtoneHelper.setSingleRingtone(theme, String.valueOf(entry.getContactId()));
            } else {
                RingtoneHelper.setSingleRingtone(null, String.valueOf(entry.getContactId()));
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw e;
            }
        }
    }

    private void checkThread() {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            throw new IllegalStateException("Called in main thread.");
        }
    }

    public void markDataChanged() {
        needFilterTheme = true;
    }

    /**
     * This method may block ui thread.
     * @param number
     * @return
     */
    public int getThemeIdByNumber(String number) {
        if (mThemeFilterContacts.isEmpty()) {
            mThemeFilterContacts.addAll(fetchThemeContacts());
            update();
        } else {
            updateFilterContactsIfNeeded();
        }

        int themeId = SimpleContact.INVALID_THEME;
        for (SimpleContact contact : mThemeFilterContacts) {
            if (isMatchPhoneNumber(contact, number)) {
                themeId = contact.getThemeId();
                break;
            }
        }
        return themeId;
    }

    public synchronized void clearThemeStatus() {
        for (SimpleContact contact : mAllContacts) {
            contact.setSelected(false);
        }
    }

    public synchronized void updateThemeId(int contactId, int id) {
        for (SimpleContact contact : mAllContacts) {
            if (contactId == contact.getContactId()) {
                contact.setThemeId(id);
            }
        }
    }

    public interface LoadCallback {
        void onLoadFinish();
    }
}
