package com.honeycomb.colorphone.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.colorphone.lock.util.CommonUtils;
import com.colorphone.lock.util.FontUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactAdapter;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.ContactUtils;
import com.honeycomb.colorphone.contact.RecyclerSectionItemDecoration;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;


/**
 * Created by sundxing on 17/11/28.
 */

public abstract class ContactsActivity extends HSAppCompatActivity {
    private static final String TAG = "ContactsActivity";

    public static final String EXTRA_THEME = "contact_theme";
    private OvershootInterpolator mInter = new OvershootInterpolator(1.5f);
    private Interpolator mFadeInter = new AccelerateDecelerateInterpolator();

    private RecyclerView mFastScrollRecyclerView;
    private List<SimpleContact> mContacts = new ArrayList<>();
    private ContactAdapter mContactAdapter;
    private RecyclerSectionItemDecoration mSectionItemDecoration;
    private View mActionLayout;
    private Button mConfirmButton;
    /**
     * Default, list items could be selectable.
     */
    private boolean mSelectable = true;

    private List<View> mToolBarTransViews = new ArrayList<>(2);
    private int mLayoutTransY;
    private int mLayoutTransX = -CommonUtils.pxFromDp(56f);
    private int mHeaderOffset;

    public static void startSelect(Context context, Theme theme) {
        Intent starter = new Intent(context, ContactsSelectActivity.class);
        starter.putExtra(EXTRA_THEME, theme);
        context.startActivity(starter);
    }

    public static void startEdit(Context context) {
        Intent starter = new Intent(context, ContactsEditActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutTransY = getResources().getDimensionPixelOffset(R.dimen.contact_item_footer_offset);
        mHeaderOffset = getResources().getDimensionPixelOffset(R.dimen.contact_item_header_offset);
        setContentView(R.layout.activity_contacts);
        View navBack = findViewById(R.id.nav_back);
        navBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView textViewTitle = findViewById(R.id.nav_title);
        textViewTitle.setText(R.string.contact_theme);

        mToolBarTransViews.add(navBack);
        mToolBarTransViews.add(textViewTitle);

        mFastScrollRecyclerView = findViewById(R.id.recycler_view);
        mFastScrollRecyclerView.setItemAnimator(new SlideInRightAnimator());
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

        mActionLayout = findViewById(R.id.bottom_action_layout);
        mConfirmButton = (Button)findViewById(R.id.contact_confirm);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmed(mContacts);
            }
        });
        mConfirmButton.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        onConfigConfirmButton(mConfirmButton);
        onConfigActionView((TextView)findViewById(R.id.action_bar_op));
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public ContactAdapter getContactAdapter() {
        return mContactAdapter;
    }

    protected abstract void onConfirmed(List<SimpleContact> contacts);

    protected abstract void onConfigConfirmButton(Button confirmButton);

    protected abstract void onConfigActionView(TextView actionText);

    protected void onContactsDataReady(List<SimpleContact> contacts) {
        mContacts.clear();
        mContacts.addAll(contacts);
        Collections.sort(mContacts, new Comparator<SimpleContact>() {
            @Override
            public int compare(SimpleContact o1, SimpleContact o2) {
                return ContactUtils.compareTitles(
                        ContactUtils.getSectionName(o1.getName()),
                        ContactUtils.getSectionName(o2.getName()));
            }
        });

        mSectionItemDecoration = new RecyclerSectionItemDecoration(getResources(),mContacts);
        mSectionItemDecoration.setHeaderOffset(mHeaderOffset);
        mSectionItemDecoration.setFooterOffset(mLayoutTransY);

        mContactAdapter = new ContactAdapter(getLayoutInflater(), mContacts, R.layout.recycler_contact_row);
        mContactAdapter.setCountTriggerListener(new ContactAdapter.CountTriggerListener() {
            @Override
            public void onTrigger(int currentSelectedCount) {
                boolean enabled = currentSelectedCount > 0;
                mConfirmButton.setEnabled(enabled);
            }
        });
        mContactAdapter.setHeaderOffset(mHeaderOffset);
        mContactAdapter.setFooterOffset(mLayoutTransY);
        mContactAdapter.setItemHeight(getResources().getDimensionPixelOffset(R.dimen.contact_item_height));
        mContactAdapter.setThemeVisible(needShowThemeName());
        mFastScrollRecyclerView.addItemDecoration(mSectionItemDecoration);
        mFastScrollRecyclerView.setAdapter(mContactAdapter);
    }

    protected boolean needShowThemeName() {
        return false;
    }

    protected void switchSelectMode() {
        updateSelectMode(!mSelectable, true);
    }

    protected void updateSelectMode(final boolean selectable) {
        updateSelectMode(selectable, false);
    }

    protected void updateSelectMode(final boolean selectable, final boolean animation) {

        // Adapter handle change logic itself.
        if (mContactAdapter != null) {
            mContactAdapter.setInSelectMode(selectable);
        }

        boolean change = mSelectable != selectable;
        if (!change) {
            return;
        }
        mSelectable = selectable;

        mActionLayout.animate()
                .translationY(selectable ? 0 : mLayoutTransY)
                .setDuration(animation ? 300 : 0)
                .setInterpolator(selectable ? mInter : mFadeInter)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mConfirmButton.setClickable(selectable);
            }
        }).start();

        for (View v : mToolBarTransViews) {
            v.animate()
                    .translationX(selectable ? mLayoutTransX : 0)
                    .setDuration(animation? 300 : 0)
                    .setInterpolator(selectable ? mFadeInter : mInter)
                    .start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContactManager.getInstance().clearThemeStatus();
    }

    protected void setHeaderHint(String headerHint) {
        if (mSectionItemDecoration != null) {
            boolean changed = mSectionItemDecoration.setHeaderHint(headerHint);
            if (changed) {
                mContactAdapter.notifyDataSetChanged();
            }
        }
    }

}
