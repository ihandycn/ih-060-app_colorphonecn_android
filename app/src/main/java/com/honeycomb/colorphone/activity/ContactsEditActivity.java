package com.honeycomb.colorphone.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.contact.ContactDBHelper;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.contact.ThemeEntry;
import com.honeycomb.colorphone.util.Analytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contact edit
 */

public class ContactsEditActivity extends ContactsActivity {

    private TextView mActionView;

    ContactManager.LoadCallback mCallback = new ContactManager.LoadCallback() {
        @Override
        public void onLoadFinish() {
            onContactsDataReady(ContactManager.getInstance().getThemes(true));
            updateSelectMode(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.contact_theme);
        updateSelectMode(false);
        ContactManager.getInstance().register(mCallback);
    }

    @Override
    protected void onDestroy() {
        ContactManager.getInstance().unRegister(mCallback);
        super.onDestroy();
    }

    @Override
    protected boolean needShowThemeName() {
        return true;
    }

    @Override
    protected void onConfirmed(List<SimpleContact> contacts) {

        List<ThemeEntry> themeEntries = new ArrayList<>();
        List<SimpleContact> rootData = ContactManager.getInstance().getThemes(true);

        Iterator<SimpleContact> iterator = contacts.iterator();
        while (iterator.hasNext()) {
            SimpleContact c = iterator.next();
            if (c.isSelected()) {
                int pos = contacts.indexOf(c);

                List<ThemeEntry> entries = ThemeEntry.valueOf(c, ContactDBHelper.Action.DELETE);
                themeEntries.addAll(entries);

                iterator.remove();
                rootData.remove(c);

                c.setThemeId(SimpleContact.INVALID_THEME);

                getContactAdapter().notifyItemRemoved(pos);
            }
        }

        if (rootData.isEmpty()) {
            updateSelectMode(false, true);
            setEmptyPlaceHolder(true);
        }
        Analytics.logEvent("Settings_ContactTheme_DeletedContactSuc");

        ContactManager.getInstance().updateDb(themeEntries, null);
    }

    @Override
    protected void onConfigConfirmButton(Button confirmButton) {
        confirmButton.setEnabled(false);
        confirmButton.setBackgroundResource(R.drawable.btn_bg_red);
        confirmButton.setText(R.string.delete);
        confirmButton.setTextColor(Color.WHITE);
    }

    @Override
    protected void onConfigActionView(TextView actionText) {
        mActionView = actionText;
        actionText.setVisibility(View.VISIBLE);
        actionText.setText(R.string.cancel);
        actionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSelectMode();
            }
        });
    }

    @Override
    protected void updateSelectMode(boolean selectable, boolean animation) {
        super.updateSelectMode(selectable, animation);
        mActionView.setText(selectable ? R.string.cancel : R.string.edit);
    }

    @Override
    public void onBackPressed() {
        if (isSelectable()) {
            switchSelectMode();
            return;
        }
        super.onBackPressed();
    }
}
