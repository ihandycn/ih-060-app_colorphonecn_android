package com.honeycomb.colorphone.dialer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

public class DialerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ComponentName componentName = intent.getComponent();

        Intent seekIntent = new Intent(Intent.ACTION_DIAL);
        seekIntent.setData(Uri.parse("tel:911"));
        PackageManager pm = getPackageManager();
        List<ResolveInfo> intentResolvers = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : intentResolvers) {
            String packageName = resolveInfo.activityInfo.packageName;
            Log.d("Dilaer-ResolveInfo", "packageName : " + packageName);
            if (!getPackageName().equals(packageName)) {
                intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
                        resolveInfo.activityInfo.name);
                startActivity(intent);
                finish();
                break;
            }
        }

    }
}
