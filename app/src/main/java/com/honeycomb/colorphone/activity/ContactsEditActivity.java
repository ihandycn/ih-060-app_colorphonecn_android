package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.contact.ContactDBHelper;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.contact.ThemeEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO Activity销毁 状态保存和恢复
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

                ThemeEntry entry = ThemeEntry.valueOf(c);
                entry.mAction = ContactDBHelper.Action.DELETE;
                themeEntries.add(entry);

                iterator.remove();
                rootData.remove(c);

                c.setThemeId(SimpleContact.INVALID_THEME);

                getContactAdapter().notifyItemRemoved(pos);
            }
        }


        // TODO progress bar ？
        ContactManager.getInstance().updateDb(themeEntries, new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(ContactsEditActivity.this, "TEST_delete_success!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onConfigConfirmButton(Button confirmButton) {
        confirmButton.setEnabled(false);
        confirmButton.setBackgroundResource(R.drawable.btn_bg_red);
        confirmButton.setText(R.string.delete);
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
