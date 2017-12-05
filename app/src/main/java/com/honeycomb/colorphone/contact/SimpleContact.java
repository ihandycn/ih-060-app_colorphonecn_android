package com.honeycomb.colorphone.contact;

import android.telephony.PhoneNumberUtils;

/**
 * Created by sundxing on 17/11/29.
 */

public class SimpleContact {
    private String mName;
    private String mRawNumber;
    private String mPhotoUri;
    private int mThemeId;

    private boolean mSelected;

    public SimpleContact() {
    }

    public SimpleContact(String name, String rawNumber, String photoUri) {
        mName = name;
        mRawNumber = rawNumber;
        mPhotoUri = photoUri;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRawNumber() {
        return mRawNumber;
    }

    public void setRawNumber(String rawNumber) {
        mRawNumber = rawNumber;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public int getThemeId() {
        return mThemeId;
    }

    public void setThemeId(int themeId) {
        mThemeId = themeId;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    @Override
    public String toString() {
       return  "photo uri = " + mPhotoUri +
               ", name = " + mName +
               ", number = " + mRawNumber +
               ", theme id = " + mThemeId;
    }

    @Override
    public boolean equals(Object obj) {
        return PhoneNumberUtils.compare(mRawNumber, ((SimpleContact)obj).getRawNumber());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
