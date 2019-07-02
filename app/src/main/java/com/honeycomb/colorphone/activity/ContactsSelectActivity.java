package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.preview.ThemePreviewView;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Activity销毁 状态保存和恢复
 */

public class ContactsSelectActivity extends ContactsActivity {


    private Theme mTheme;
    ContactManager.LoadCallback mCallback = new ContactManager.LoadCallback() {
        @Override
        public void onLoadFinish() {
            onContactsDataReady(ContactManager.getInstance().getThemes(false));
            updateSelectMode(true);
            //setHeaderHint(getString(R.string.contact_select_hint, mTheme.getName()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTheme = (Theme) getIntent().getSerializableExtra(EXTRA_THEME);
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.select_contacts);

        ContactManager.getInstance().register(mCallback);

        /**
         * Clear cached contact
         */
        ThemeSetHelper.cacheContactList(null);

    }

    @Override
    protected void onDestroy() {
        ContactManager.getInstance().unRegister(mCallback);
        super.onDestroy();
    }

    @Override
    protected void onConfirmed(List<SimpleContact> contacts) {
        final List<SimpleContact> selectContacts = new ArrayList<>();
        for (SimpleContact c : contacts) {
            if (c.isSelected()) {
               selectContacts.add(c);
            }
        }

        if (mTheme.hasRingtone()) {
            ContactsSelectActivity.this.finish();
            ThemeSetHelper.cacheContactList(selectContacts);
        } else {
            ThemeSetHelper.onConfirm(selectContacts, mTheme, new Runnable() {
                @Override
                public void run() {
                    ContactsSelectActivity.this.finish();
                    ThemePreviewView.sThemeApplySuccessFlag = true;
                }
            });
        }
    }

    @Override
    protected void onConfigConfirmButton(Button confirmButton) {
        confirmButton.setEnabled(false);
        confirmButton.setBackgroundResource(R.drawable.btn_bg_yellow_disabled);
        confirmButton.setText(R.string.set);
        confirmButton.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.seletor_color_cotact_btn_txt, null));
    }

    @Override
    protected void onConfigActionView(TextView actionText) {
        actionText.setVisibility(View.GONE);
    }
}
