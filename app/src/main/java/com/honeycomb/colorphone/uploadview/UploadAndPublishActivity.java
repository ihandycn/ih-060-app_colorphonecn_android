package com.honeycomb.colorphone.uploadview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.ViewPagerFixed;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import hugo.weaving.DebugLog;

public class UploadAndPublishActivity extends HSAppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private LayoutInflater mLayoutInflater;
    private ViewPagerFixed mViewPager;
    private ImageView mBackIconButton;
    private TextView mAlreadyUploadButton;
    private TextView mAlreadyPublishButton;
    private TextView mVideoEditButton;

    public boolean isEditState = false;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_upload_video);

        mViewPager = findViewById(R.id.upload_viewpager);
        mBackIconButton = findViewById(R.id.back_icon);
        mAlreadyUploadButton = findViewById(R.id.already_upload);
        mAlreadyPublishButton = findViewById(R.id.already_publish);
        mVideoEditButton = findViewById(R.id.video_edit);

        mBackIconButton.setOnClickListener(this);
        mAlreadyUploadButton.setOnClickListener(this);
        mAlreadyPublishButton.setOnClickListener(this);
        mVideoEditButton.setOnClickListener(this);

        mLayoutInflater = LayoutInflater.from(this);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    return mLayoutInflater.inflate(R.layout.upload_video_layout, container, false);
                } else {
                    return mLayoutInflater.inflate(R.layout.publish_video_layout, container, false);
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_icon) {
            finish();
        } else if (view.getId() == R.id.already_upload) {
            mViewPager.setCurrentItem(0);
        } else if (view.getId() == R.id.already_publish) {
            mViewPager.setCurrentItem(1);
        } else if (view.getId() == R.id.video_edit) {
            if (mViewPager.getCurrentItem() == 0) {
                if (!isEditState) {
                    isEditState = true;
                    mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.cancel));
                    HSGlobalNotificationCenter.sendNotification("upload_edit");
                } else {
                    isEditState = false;
                    mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.edit));
                    HSGlobalNotificationCenter.sendNotification("upload_cancel");
                }
            } else if (mViewPager.getCurrentItem() == 1) {
                if (!isEditState) {
                    isEditState = true;
                    mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.cancel));
                    HSGlobalNotificationCenter.sendNotification("publish_edit");
                } else {
                    isEditState = false;
                    mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.edit));
                    HSGlobalNotificationCenter.sendNotification("publish_cancel");
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        isEditState = false;
        mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.edit));
        if (position == 0) {
            HSGlobalNotificationCenter.sendNotification("publish_cancel");
        } else if (position == 1) {
            HSGlobalNotificationCenter.sendNotification("upload_cancel");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
