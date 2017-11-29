package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import hugo.weaving.DebugLog;


/**
 * Created by sundxing on 17/11/28.
 */

public class ContactsActivity extends HSAppCompatActivity {
    private static final String TAG = "ContactsActivity";

    public static void start(Context context) {
        Intent starter = new Intent(context, ContactsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);
        readContacts();
    }

    @DebugLog
    public void readContacts() {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                String thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Log.d(TAG, "photo uri = " + thumbnailUri + ", name = " + name + ", number = " + number);
            }
            boolean result = PhoneNumberUtils.compare("(656) 889-96","65688996");

            Log.d(TAG, "(656) 889-96 =  65688996 ? " + result);
            Log.d(TAG, "Format 15896633258 : " + PhoneNumberUtils.formatNumber("15896633258"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
