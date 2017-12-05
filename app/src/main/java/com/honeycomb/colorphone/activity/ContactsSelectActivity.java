package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.ContactDBHelper;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.contact.ThemeEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Activity销毁 状态保存和恢复
 */

public class ContactsSelectActivity extends ContactsActivity {

    private Theme mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTheme = (Theme) getIntent().getSerializableExtra(EXTRA_THEME);
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.contact_theme);

        ContactManager.getInstance().getLocalContacts(new ContactManager.LoadCallback() {
            @Override
            public void onLoadFinish(List<SimpleContact> contacts) {
                onContactsDataReady(contacts);
                updateSelectMode(true);
                setHeaderHint(getString(R.string.contact_select_hint, mTheme.getName()));
            }
        });

    }

    @Override
    protected void onConfirmed(List<SimpleContact> contacts) {

        List<ThemeEntry> themeEntries = new ArrayList<>();

        for (SimpleContact c : contacts) {
            if (c.isSelected()) {
                c.setThemeId(mTheme.getId());
                ThemeEntry entry = ThemeEntry.valueOf(c);
                entry.mAction = ContactDBHelper.Action.INSERT;
                if (ContactManager.getInstance().themeSetAlready(c)) {
                    entry.mAction = ContactDBHelper.Action.UPDATE;
                }
                themeEntries.add(entry);
            }
        }

        // TODO progress bar ？
        ContactManager.getInstance().updateDb(themeEntries, new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(ContactsSelectActivity.this, "TEST_update_success!", Toast.LENGTH_SHORT).show();
                }
                ContactsSelectActivity.this.finish();
            }
        });
    }

    @Override
    protected void onConfigConfirmButton(Button confirmButton) {
        confirmButton.setEnabled(false);
        confirmButton.setBackgroundResource(R.drawable.btn_bg_yellow_disabled);
        confirmButton.setText(R.string.set);
    }

    @Override
    protected void onConfigActionView(TextView actionText) {
        actionText.setVisibility(View.GONE);
    }
}
