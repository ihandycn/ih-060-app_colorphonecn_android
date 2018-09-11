package com.honeycomb.colorphone.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;
import java.util.List;


public class PopularThemePreviewActivity extends ThemePreviewActivity {

    public static void start(Context context, int position) {
        Intent starter = new Intent(context, PopularThemePreviewActivity.class);
        starter.putExtra("position", position);
        if (context instanceof Activity) {
            ((Activity)context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        }
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected List<Theme> getThemes() {
        List<Theme> list = new ArrayList<>();
        for (Theme theme : Theme.themes()) {
            if (theme.isSpecialTopic()) {
                list.add(theme);
            }
        }
        return list;
    }
}
