package com.honeycomb.colorphone.contact;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.ihs.commons.utils.HSLog;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static boolean isSectionNameMiscOrDigit(String sectionName) {
        return AlphabeticIndexCompat.getInstance().isSectionNameMiscOrDigit(sectionName);
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
                ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        List<SimpleContact> mContacts = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                Log.d("Read Contact", "photo uri = " + thumbnailUri + ", name = " + name + ", number = " + number + ", contactId =" + contactId);
                if (!TextUtils.isEmpty(number)) {
                    if (TextUtils.isEmpty(name)) {
                        // If Name is empty we use Number as User Name.
                        name = number;
                    }
                    addNewContact(mContacts, name, number, thumbnailUri, contactId);
                }
            }

        } catch (Exception e) {
            HSLog.e("Contact", "log err:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mContacts;
    }

    public static void addNewContact(List<SimpleContact> mContacts, String name, String number, String thumbnailUri, int contactId) {
        for (SimpleContact contact : mContacts) {
            if (contact.getContactId() == contactId) {
                contact.addOtherPhoneNumber(number);
                return;
            }
        }
        mContacts.add(new SimpleContact(name, number, thumbnailUri, contactId));
    }


    /**
     * Load a contact photo thumbnail and return it as a Bitmap,
     * resizing the image to the provided image dimensions as needed.
     *
     * @param photoData photo ID Prior to Honeycomb, the contact's _ID value.
     *                  For Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
     * @return A thumbnail Bitmap, sized to the provided width and height.
     * Returns null if the thumbnail is not found.
     */
    public static Bitmap loadContactPhotoThumbnail(Context context, String photoData) {
        // Creates an asset file descriptor for the thumbnail file.
        AssetFileDescriptor afd = null;
        // try-catch block for file not found
        try {
            // Creates a holder for the URI.
            Uri thumbUri;
            // If Android 3.0 or later
            thumbUri = Uri.parse(photoData);

        /*
         * Retrieves an AssetFileDescriptor object for the thumbnail
         * URI
         * using ContentResolver.openAssetFileDescriptor
         */
            afd = context.getContentResolver().
                    openAssetFileDescriptor(thumbUri, "r");
        /*
         * Gets a file descriptor from the asset file descriptor.
         * This object can be used across processes.
         */
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            // Decode the photo file and return the result as a Bitmap
            // If the file descriptor is valid
            if (fileDescriptor != null) {
                // Decodes the bitmap
                return BitmapFactory.decodeFileDescriptor(
                        fileDescriptor, null, null);
            }
            // If the file isn't found
        } catch (FileNotFoundException e) {
            /*
             * Handle file not found errors
             */
            // In all cases, close the asset file descriptor
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
