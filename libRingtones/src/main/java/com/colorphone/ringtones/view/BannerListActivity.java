package com.colorphone.ringtones.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.colorphone.ringtones.R;
import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.RingtoneImageLoader;
import com.colorphone.ringtones.RingtoneManager;
import com.colorphone.ringtones.RingtoneSetDelegate;
import com.colorphone.ringtones.module.Banner;
import com.ihs.app.framework.activity.HSAppCompatActivity;

/**
 * @author sundxing
 */
public class BannerListActivity extends HSAppCompatActivity {

    private Banner mBanner;
    private RingtoneSetDelegate mRingtoneSetDelegate;

    public static void start(Context context, Banner banner) {
        Intent starter = new Intent(context, BannerListActivity.class);
        starter.putExtra("banner", banner);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ringtone_banner_list);
        mBanner = (Banner) getIntent().getSerializableExtra("banner");
        findViewById(R.id.nav_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ImageView imageView = findViewById(R.id.banner_image);
        RingtoneImageLoader imageLoader = RingtoneConfig.getInstance().getRingtoneImageLoader();
        imageLoader.loadImage(
                this,
                mBanner.getImgUrl(),
                imageView,
                R.drawable.ringtone_item_cover_default);

        RecyclerView recyclerView = findViewById(R.id.banner_ringtone_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));

        recyclerView.setAdapter(new RingtoneListAdapter(this, new RingtoneApi(), mBanner.getColumnId(), false));

        mRingtoneSetDelegate = new RingtoneSetDelegate(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        RingtoneManager.getInstance().setRingtoneSetHandler(mRingtoneSetDelegate);
        mRingtoneSetDelegate.onStart();
    }

    @Override
    public void onBackPressed() {
        if (!mRingtoneSetDelegate.handleBackPress()) {
            super.onBackPressed();
        }
    }
}
