package com.honeycomb.colorphone.boost;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;

public class AppInfo {
    private ApplicationInfo aInfo;
    private String name;
    private char first;
    private String packageName;
    private String launchActivityName;
    private boolean isSelected = false;
    private AlphabeticIndexCompat mIndexer;
    private Drawable icon;

    public AppInfo(ApplicationInfo info, boolean selected) {
        aInfo = info;
        packageName = aInfo.packageName;

        isSelected = selected;

        if (0 != aInfo.labelRes) {
            name = String.valueOf(aInfo.loadLabel(HSApplication.getContext().getPackageManager()));
        } else if (!TextUtils.isEmpty(aInfo.name)) {
            name = aInfo.name;
        } else {
            name = aInfo.packageName;
        }

        mIndexer = new AlphabeticIndexCompat(HSApplication.getContext());
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

    public ApplicationInfo getAInfo() {
        return aInfo;
    }

    public Drawable getIcon() {
        if (icon == null) {
            try {
                icon = aInfo.loadIcon(HSApplication.getContext().getPackageManager());
            } catch (Resources.NotFoundException e) {
            } catch (OutOfMemoryError e) {
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
        return first;
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
        return AppInfoHolder.getInstance().getIntent(comp);
    }

}
