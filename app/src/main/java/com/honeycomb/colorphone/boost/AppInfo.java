package com.honeycomb.colorphone.boost;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;

import java.lang.ref.WeakReference;

public class AppInfo {
    private ApplicationInfo aInfo;
    private PackageInfo packageInfo;
    private String name;
    private char first = 0;
    private String packageName;
    private String launchActivityName;
    private boolean isSelected = false;
    private AlphabeticIndexCompat mIndexer;
    private WeakReference<Drawable> mIconReference;

    public AppInfo(PackageInfo info, boolean selected) {
        packageInfo = info;
        aInfo = info.applicationInfo;
        packageName = aInfo.packageName;

        isSelected = selected;

        if (0 != aInfo.labelRes) {
            name = String.valueOf(aInfo.loadLabel(HSApplication.getContext().getPackageManager()));
        } else if (!TextUtils.isEmpty(aInfo.name)) {
            name = aInfo.name;
        } else {
            name = aInfo.packageName;
        }
    }

    public ApplicationInfo getAInfo() {
        return aInfo;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public Drawable getIcon() {
        Drawable icon = null;
        if (mIconReference != null) {
            icon = mIconReference.get();
        }
        if (icon == null && aInfo != null) {
            try {
                icon = aInfo.loadIcon(HSApplication.getContext().getPackageManager());
                mIconReference = new WeakReference<Drawable>(icon);
            } catch (Resources.NotFoundException | IllegalArgumentException e) {
                // do nothing
            }
        }
        return icon;
    }

    public String getName() {
        return name;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public char getFirst() {
        ensureFirstChar();
        return first;
    }

    private void ensureFirstChar() {
        if (first != 0) {
            return;
        }
        if (mIndexer == null) {
            mIndexer = new AlphabeticIndexCompat(HSApplication.getContext());
        }
        String sectionName = mIndexer.computeSectionName(name.trim());
        // If invoke method failed, section name may be empty.
        if (!TextUtils.isEmpty(sectionName)) {
            first = sectionName.charAt(0);
            if (first < 'A' || first > 'Z') {
                first = '#';
            }
        } else {
            first = '#';
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getLaunchActivityName() {
        return launchActivityName;
    }

    public void setLaunchActivityName(String name) {
        launchActivityName = name;
    }

    @Override
    public String toString() {
        return "AppInfo [aInfo=" + aInfo + ", name=" + name + ", first=" + first + ", packageName=" + packageName + ", launchActivityName=" + launchActivityName + ", isSelected="
                + isSelected + "]";
    }

    public void resetState(AppInfo changed) {
        isSelected = changed.isSelected;
    }

    public Intent getIntent() {
        ComponentName comp = new ComponentName(getPackageName(), getLaunchActivityName());
        return AppInfoHolder.getIntent(comp);
    }

}
