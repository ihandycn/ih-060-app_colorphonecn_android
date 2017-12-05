package com.honeycomb.colorphone.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by sundxing on 17/11/29.
 */

public class ContactUtils {

    public static String getSectionName(CharSequence target) {
        return AlphabeticIndexCompat.getInstance().getSectionName(target);
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    public static int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return Collator.getInstance().compare(titleA, titleB);
    }

    @DebugLog
    public static List<SimpleContact> readAllContacts(Context context) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        List<SimpleContact> mContacts = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Log.d("Read Contact", "photo uri = " + thumbnailUri + ", name = " + name + ", number = " + number);
                if (!TextUtils.isEmpty(number)) {
                    mContacts.add(new SimpleContact(name, number, thumbnailUri));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mContacts;
    }
}
