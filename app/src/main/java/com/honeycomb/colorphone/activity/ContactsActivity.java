package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.colorphone.lock.util.CommonUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.contact.ContactAdapter;
import com.honeycomb.colorphone.contact.ContactUtils;
import com.honeycomb.colorphone.contact.RecyclerSectionItemDecoration;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hugo.weaving.DebugLog;


/**
 * Created by sundxing on 17/11/28.
 * TODO: filter contact number empty.
 */

public class ContactsActivity extends HSAppCompatActivity {
    private static final String TAG = "ContactsActivity";

    private RecyclerView mFastScrollRecyclerView;
    private List<SimpleContact> mContacts = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, ContactsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);
        findViewById(R.id.nav_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.contact_theme);
        mFastScrollRecyclerView = findViewById(R.id.recycler_view);
        int padding  = getResources().getDimensionPixelSize(R.dimen.recycler_section_header_Margin);
        if (CommonUtils.isRtl()) {
            mFastScrollRecyclerView.setPadding(0
                    , 0, padding, 0);
        } else {
            mFastScrollRecyclerView.setPadding(padding
                    , 0, 0, 0);
        }
        mFastScrollRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        readContacts();

        Collections.sort(mContacts, new Comparator<SimpleContact>() {
            @Override
            public int compare(SimpleContact o1, SimpleContact o2) {
                return ContactUtils.compareTitles(
                        ContactUtils.getSectionName(o1.getName()),
                        ContactUtils.getSectionName(o2.getName()));
            }
        });


        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources(),
                        mContacts.size(),
                        getSectionCallback(mContacts));

        mFastScrollRecyclerView.setAdapter(new ContactAdapter(getLayoutInflater(), mContacts, R.layout.recycler_contact_row));
        mFastScrollRecyclerView.addItemDecoration(sectionItemDecoration);
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<SimpleContact> people) {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 0
                        || !TextUtils.equals(getSectionHeader(position), getSectionHeader(position - 1));
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return ContactUtils.getSectionName(
                        people.get(position).getName()) ;
            }
        };
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
                if (!TextUtils.isEmpty(number)) {
                    mContacts.add(new SimpleContact(name, number, thumbnailUri));
                }
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
