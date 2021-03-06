package com.honeycomb.colorphone.uploadview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.VideoManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.MediaSharedElementCallback;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.view.ViewPagerFixed;

import hugo.weaving.DebugLog;

public class UploadAndPublishActivity extends HSAppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, INotificationObserver {

    private LayoutInflater mLayoutInflater;
    private ViewPagerFixed mViewPager;
    private ImageView mBackIconButton;
    private TextView mAlreadyUploadButton;
    private TextView mAlreadyPublishButton;
    private TextView mVideoEditButton;
    private UploadVideoView mUploadView;
    private PublishVideoView mPublishView;

    public boolean isEditState = false;

    private boolean isShowEditButtonOnUploadPage = false;
    private boolean isShowEditButtonOnPublishPage = false;

    private MediaSharedElementCallback sharedElementCallback;

    public static void start(Context context) {
        Intent starter = new Intent(context, UploadAndPublishActivity.class);
        context.startActivity(starter);
    }

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

        mVideoEditButton.setVisibility(View.GONE);

        mBackIconButton.setOnClickListener(this);
        mAlreadyUploadButton.setOnClickListener(this);
        mAlreadyPublishButton.setOnClickListener(this);
        mVideoEditButton.setOnClickListener(this);

        mLayoutInflater = LayoutInflater.from(this);

        // Transition
        sharedElementCallback = new MediaSharedElementCallback();
        sharedElementCallback.setClearAfterConsume(true);
        ActivityCompat.setExitSharedElementCallback(this, sharedElementCallback);

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
                    mUploadView = (UploadVideoView) mLayoutInflater.inflate(R.layout.upload_video_layout, container, false);
                    mUploadView.setSharedElementCallback(sharedElementCallback);
                    container.addView(mUploadView);
                    return mUploadView;
                } else {
                    mPublishView = (PublishVideoView) mLayoutInflater.inflate(R.layout.publish_video_layout, container, false);
                    mPublishView.setSharedElementCallback(sharedElementCallback);
                    container.addView(mPublishView);
                    return mPublishView;
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

            }
        });
        mViewPager.addOnPageChangeListener(this);
        HSGlobalNotificationCenter.addObserver("no_upload_data", this);
        HSGlobalNotificationCenter.addObserver("no_publish_data", this);
        HSGlobalNotificationCenter.addObserver("have_upload_data", this);
        HSGlobalNotificationCenter.addObserver("have_publish_data", this);
        HSGlobalNotificationCenter.addObserver("quit_edit_mode", this);

        Analytics.logEvent("MyUploads_Page_Show");
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoManager.get().mute(true);
        if (mUploadView != null && mViewPager.getCurrentItem() == 0) {
            mUploadView.onResume();
        }

        if (mPublishView != null && mViewPager.getCurrentItem() == 1) {
            mPublishView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUploadView != null && mViewPager.getCurrentItem() == 0) {
            mUploadView.onPause();
        }

        if (mPublishView != null && mViewPager.getCurrentItem() == 1) {
            mPublishView.onPause();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            final int exitPos = data.getIntExtra("index", -1);
            if (mUploadView != null && mViewPager.getCurrentItem() == 0) {
                mUploadView.onActivityReenter(exitPos);
            } else if (mPublishView != null && mViewPager.getCurrentItem() == 1) {
                mPublishView.onActivityReenter(exitPos);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
        ThemeList.getInstance().clearPublishData();
        ThemeList.getInstance().clearUploadData();
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
            if (isShowEditButtonOnUploadPage) {
                mVideoEditButton.setVisibility(View.VISIBLE);
            } else {
                mVideoEditButton.setVisibility(View.GONE);
            }
            mAlreadyUploadButton.setTextColor(0xffffffff);
            mAlreadyPublishButton.setTextColor(0xff615d8e);
            HSGlobalNotificationCenter.sendNotification("publish_cancel");
        } else if (position == 1) {
            if (isShowEditButtonOnPublishPage) {
                mVideoEditButton.setVisibility(View.VISIBLE);
            } else {
                mVideoEditButton.setVisibility(View.GONE);
            }
            mAlreadyUploadButton.setTextColor(0xff615d8e);
            mAlreadyPublishButton.setTextColor(0xffffffff);
            HSGlobalNotificationCenter.sendNotification("upload_cancel");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if ("no_upload_data".equals(s)) {
            isShowEditButtonOnUploadPage = false;
            if (mViewPager.getCurrentItem() == 0 && mVideoEditButton != null) {
                mVideoEditButton.setVisibility(View.GONE);
            }
        } else if ("no_publish_data".equals(s)) {
            isShowEditButtonOnPublishPage = false;
            if (mViewPager.getCurrentItem() == 1 && mVideoEditButton != null) {
                mVideoEditButton.setVisibility(View.GONE);
            }
        } else if ("have_upload_data".equals(s)) {
            isShowEditButtonOnUploadPage = true;
            if (mViewPager.getCurrentItem() == 0 && mVideoEditButton != null) {
                mVideoEditButton.setVisibility(View.VISIBLE);
            }
        } else if ("have_publish_data".equals(s)) {
            isShowEditButtonOnPublishPage = true;
            if (mViewPager.getCurrentItem() == 1 && mVideoEditButton != null) {
                mVideoEditButton.setVisibility(View.VISIBLE);
            }
        } else if ("quit_edit_mode".equals(s)) {
            isEditState = false;
            mVideoEditButton.setText(getBaseContext().getResources().getString(R.string.edit));
        }
    }
}
