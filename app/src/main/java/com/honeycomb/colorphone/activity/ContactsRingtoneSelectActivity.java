package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.module.Ringtone;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.superapps.util.Navigations;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Activity销毁 状态保存和恢复
 * @author sundxing
 */

public class ContactsRingtoneSelectActivity extends ContactsActivity {

    public static void startSelectRingtone(Context context, Ringtone ringtone) {
        Intent starter = new Intent(context, ContactsRingtoneSelectActivity.class);
        starter.putExtra(EXTRA_THEME, ringtone);
        Navigations.startActivitySafely(context, starter);
    }

    private Ringtone mRingtone;
    ContactManager.LoadCallback mCallback = new ContactManager.LoadCallback() {
        @Override
        public void onLoadFinish() {
            List<SimpleContact> simpleContacts =ContactManager.getInstance().getThemes(false);
            List<SimpleContact> targetList = new ArrayList<>(simpleContacts.size());
            // Deep copy , clear Video Theme
            for (SimpleContact contact : simpleContacts) {
                SimpleContact raw = new SimpleContact(contact.getName(), contact.getRawNumber(), contact.getPhotoUri(),contact.getContactId());
                targetList.add(raw);
            }
            onContactsDataReady(targetList);
            updateSelectMode(true);
            //setHeaderHint(getString(R.string.contact_select_hint, mTheme.getName()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRingtone = (Ringtone) getIntent().getSerializableExtra(EXTRA_THEME);
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.select_contacts);

        ContactManager.getInstance().register(mCallback);

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

        ContactsRingtoneSelectActivity.this.finish();
        ThemeSetHelper.setContactRingtone(mRingtone, selectContacts);

        RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_SetForContact_Success",
                "Name", mRingtone.getTitle(),
                "Type:", mRingtone.getColumnSource());
        RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_Set_Success",
                "Name", mRingtone.getTitle(),
                "Type:", mRingtone.getColumnSource());
        Toasts.showToast("设置成功");
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
