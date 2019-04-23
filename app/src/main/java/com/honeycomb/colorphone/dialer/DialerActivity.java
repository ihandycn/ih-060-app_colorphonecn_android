package com.honeycomb.colorphone.dialer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.honeycomb.colorphone.util.Analytics;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DialerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        Intent seekIntent = new Intent(Intent.ACTION_DIAL);
        seekIntent.setData(Uri.parse("tel:911"));
        PackageManager pm = getPackageManager();
        List<ResolveInfo> intentResolvers = pm.queryIntentActivities(seekIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean hasTarget = false;
        for (ResolveInfo resolveInfo : intentResolvers) {
            String packageName = resolveInfo.activityInfo.packageName;
            Log.d("Dialer-ResolveInfo", "packageName : " + packageName);
            if (!getPackageName().equals(packageName)) {
                intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
                        resolveInfo.activityInfo.name);
                startActivity(intent);
                hasTarget = true;
                finish();
                break;
            }
        }

        if (!hasTarget) {
            Analytics.logEvent("Dialer_Launch_Failed");
        }

    }
}
