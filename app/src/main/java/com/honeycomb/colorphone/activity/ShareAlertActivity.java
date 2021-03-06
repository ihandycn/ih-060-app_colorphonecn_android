package com.honeycomb.colorphone.activity;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.LEDAnimationView;
import com.acb.call.themes.Type;
import com.acb.call.utils.FileUtils;
import com.acb.call.views.CircleImageView;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.receiver.ShareReceiver;
import com.honeycomb.colorphone.util.ShareAlertAutoPilotUtils;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.FixRatioPreviewWindow;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class ShareAlertActivity extends Activity {

    private static final String TAG = ShareAlertActivity.class.getSimpleName();

    public static class UserInfo implements Serializable {
        private String phoneNumber;
        private String callName;
        private String photoUri;

        public UserInfo(String phoneNumber, String callName, String photoUri) {
            this.phoneNumber = phoneNumber;
            this.callName = callName;
            this.photoUri = photoUri;
        }

        public String getCallName() {
            return callName;
        }

        public void setCallName(String callName) {
            this.callName = callName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPhotoUri() {
            return photoUri;
        }

        public void setPhotoUri(String photoUri) {
            this.photoUri = photoUri;
        }
    }

    public static final String PREFS_FILE = "share_alert_prefs_file_name";
    public static final String SHARE_ALERT_IN_APP_SHOW_COUNT = "share_alert_in_show_count";
    public static final String SHARE_ALERT_IN_APP_SHOW_TIME = "share_alert_in_show_time";
    public static final String SHARE_ALERT_OUT_APP_SHOW_COUNT = "share_alert_out_show_count";
    public static final String SHARE_ALERT_OUT_APP_SHOW_TIME = "share_alert_out_show_time";

    public static final String IS_INSIDE_APP = "is inside_app";
    public static final String USER_INFO = "user_info";
    public static final String SET_FOR_MULTI = "set_for_multi";
    private static final int REQUEST_SHARE = 3;

    private ThemePreviewWindow themePreviewWindow;
    private InCallActionView inCallActionView;
    private UserInfo userInfo;

    private Type themeType;
    private boolean isContactInApp;
    private boolean isInsideApp;
    private boolean isSetForSomeone;
    private boolean v22 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    private interface BitmapFetcher {
        void onResourceReady(Bitmap resource);
    }

    public static void starInsideApp(Activity activity, UserInfo userInfo, boolean setForMulti) {
        Intent intent = new Intent(activity, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_INSIDE_APP, true);
        intent.putExtra(USER_INFO, userInfo);
        intent.putExtra(SET_FOR_MULTI, setForMulti);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        Preferences helper = Preferences.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_IN_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_IN_APP_SHOW_COUNT);
    }

    public static void startOutsideApp(Context context, UserInfo userInfo) {
        Intent intent = new Intent(context, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(IS_INSIDE_APP, false);
        intent.putExtra(USER_INFO, userInfo);
        context.startActivity(intent);
        Preferences helper = Preferences.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_OUT_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_OUT_APP_SHOW_COUNT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        isInsideApp = intent.getBooleanExtra(IS_INSIDE_APP, true);
        userInfo = (UserInfo) intent.getSerializableExtra(USER_INFO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        initThemePreviewWindow();
        initShareAlertText();
        initShareButton();
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        themePreviewWindow.playAnimation(themeType);
        inCallActionView.setTheme(themeType);
    }

    @Override
    protected void onPause() {
        super.onPause();
        themePreviewWindow.stopAnimations();
        inCallActionView.stopAnimations();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SHARE:
                HSLog.d(TAG, "Request share over");
                Utils.deleteRecursive(getTempShareDirectory(this));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Utils.deleteRecursive(getTempShareDirectory(this));
        super.onDestroy();
    }

    private void initThemePreviewWindow() {
        themePreviewWindow = findViewById(R.id.card_flash_preview_window);
        inCallActionView = findViewById(R.id.card_in_call_action_view);
        inCallActionView.setEnabled(false);

        int themeID = ScreenFlashManager.getInstance().getAcbCallFactory().getIncomingReceiverConfig().getThemeIdByPhoneNumber(userInfo == null ? null : userInfo.getPhoneNumber());
        themeType = com.acb.utils.Utils.getTypeByThemeId(themeID);

        FixRatioPreviewWindow cardView = findViewById(R.id.card_view);
        cardView.setAspectRatioAndInvalidate(0.6521f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setTranslationZ(3f);
        }
        themePreviewWindow.updateThemeLayout(themeType);
        themePreviewWindow.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);

        final TextView firstLineTextView = themePreviewWindow.findViewById(R.id.first_line);
        final TextView secondLineTextView = themePreviewWindow.findViewById(R.id.second_line);
        firstLineTextView.post(new Runnable() {
            @Override
            public void run() {
                firstLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                secondLineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);

            }
        });

        isSetForSomeone = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) != themeID;
        editUserView(themePreviewWindow);
    }

    private void editUserView(View root) {
        CircleImageView portrait = root.findViewById(com.acb.call.R.id.caller_avatar);
        TextView firstLineTextView = root.findViewById(R.id.first_line);
        TextView secondLineTextView = root.findViewById(R.id.second_line);


        float shadowOffset = Dimensions.pxFromDp(2);
        firstLineTextView.setShadowLayer(shadowOffset, 0, shadowOffset, Color.BLACK);
        secondLineTextView.setShadowLayer(shadowOffset * 0.5f, 0, shadowOffset * 0.7f, Color.BLACK);

        firstLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getBondFont());
        secondLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getNormalFont());

        if (userInfo == null || getIntent().getBooleanExtra(SET_FOR_MULTI, false)) {
            firstLineTextView.setText(R.string.share_default_name);
            secondLineTextView.setText(R.string.share_default_number);
            setPortraitViewGone(portrait, root);
            return;
        }
        if (!isInsideApp || userInfo.getPhoneNumber() != null) {
            if (!TextUtils.isEmpty(userInfo.getPhotoUri())) {
                portrait.setImageURI(Uri.parse(userInfo.getPhotoUri()));
            } else {
                setPortraitViewGone(portrait, root);
            }

            if (!TextUtils.isEmpty(userInfo.getCallName())) {
                firstLineTextView.setText(userInfo.getCallName());
                isContactInApp = true;
            }
            secondLineTextView.setText(PhoneNumberUtils.formatNumber(userInfo.getPhoneNumber()));
        } else {
            setPortraitViewGone(portrait, root);
        }
    }

    private void setPortraitViewGone(ImageView portrait, View root) {
        portrait.setVisibility(View.GONE);
        if (themeType.getValue() == Type.TECH) {
            root.findViewById(R.id.caller_avatar_container).setVisibility(View.GONE);
        }
    }

    private void initShareAlertText() {
//        TextView title = findViewById(R.id.title);
//        TextView content = findViewById(R.id.content);
//        if (isInsideApp) {
//            title.setText(ShareAlertAutoPilotUtils.getInsideAppShareAlertTitle());
//            content.setText(ShareAlertAutoPilotUtils.getInsideAppShareDetail());
//        } else {
//            title.setText(ShareAlertAutoPilotUtils.getOutsideAppShareAlertTitle());
//            content.setText(ShareAlertAutoPilotUtils.getOutsideAppShareDetail());
//        }
    }

    private void initShareButton() {
        TextView shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View cardView = LayoutInflater.from(ShareAlertActivity.this).inflate(getLayout(), null, false);

                if (themeType.getValue() == Type.LED) {
                    LEDAnimationView ledAnimationView = cardView.findViewById(R.id.animation_view);
                    ledAnimationView.getLayoutParams().width = Dimensions.getPhoneWidth(ShareAlertActivity.this);
                    ledAnimationView.getLayoutParams().height = Dimensions.getPhoneHeight(ShareAlertActivity.this);
                }
                setImageCoverAndShare(cardView);
            }
        });
    }

    private boolean shareVideo(Type themeType) {
        File videoFile = new File(FileUtils.getMediaDirectory(), themeType.getFileName());

        File shareFile = null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File shareDir = getTempShareDirectory(this);
            shareFile = new File(shareDir.getAbsolutePath(),  themeType.getName() + ".mp4");
        }
        if (videoFile.exists() && shareFile != null) {
            File finalShareFile = shareFile;
            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.copyFile(videoFile, finalShareFile);
                        doShareFile(finalShareFile, "video/mp4");
                        HSLog.d(TAG, "Share video success");
                    } catch (IOException e) {
                        e.printStackTrace();
                        HSLog.e(TAG, "Share video fail!");
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void share(View cardView) {
        editUserView(cardView);
        Bitmap bitmap = getScreenViewBitmap(cardView);
        if (bitmap == null) {
            finish();
            return;
        }
        Canvas c = new Canvas(bitmap);

        try {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                File shareDir = getTempShareDirectory(this);
                String filepath = shareDir.getAbsolutePath() + "/" + themeType.getName() + ".jpg";
                File tempFile = new File(filepath);
                FileOutputStream ostream = new FileOutputStream(tempFile);
                c.drawBitmap(bitmap, 0, 0, null);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();

                File shareFile = new File(filepath);
                doShareFile(shareFile, "image/*");
            }
        } catch (IOException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
        finish();
    }

    private void doShareFile(File shareFile, String intentFormatType) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(intentFormatType);
        share.putExtra(Intent.EXTRA_TEXT,
                isInsideApp ? ShareAlertAutoPilotUtils.getInsideAppShareText()
                        : ShareAlertAutoPilotUtils.getOutsideAppShareText());
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent receiver = new Intent(this, ShareReceiver.class);
            receiver.putExtra(IS_INSIDE_APP, isInsideApp);
            receiver.putExtra(ShareReceiver.THEME_NAME, themeType.getName());
            receiver.putExtra(ShareReceiver.IS_SET_FOR_SOMEONE, isSetForSomeone);
            receiver.putExtra(ShareReceiver.IS_CONTACT, isContactInApp);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_SHARE, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent chooser = Intent.createChooser(share, getResources().getString(R.string.app_name), pendingIntent.getIntentSender());
            startActivityForResult(chooser, REQUEST_SHARE);
        } else {
            Intent chooser = Intent.createChooser(share, getResources().getString(R.string.app_name));
            startActivityForResult(chooser, REQUEST_SHARE);
        }
    }

    private void setImageCoverAndShare(final View root) {
        if (themeType.getValue() == Type.TECH || themeType.getValue() == Type.LED) {
            share(root);
            return;
        }

        if (themeType.isVideo() && shareVideo(themeType)) {
            return;
        }

        final ImageView previewImage = root.findViewById(R.id.preview_image);
        getBitmap(themeType.getPreviewImage(), Dimensions.getPhoneWidth(this), Dimensions.getPhoneHeight(this), new BitmapFetcher() {
            @Override
            public void onResourceReady(Bitmap resource) {
                previewImage.setImageBitmap(resource);
                getActionViewBitmap(root, new BitmapFetcher() {
                    @Override
                    public void onResourceReady(Bitmap resource) {
                        share(root);
                    }
                });

            }
        });
    }

    private Bitmap getScreenViewBitmap(View v) {
        v.setDrawingCacheEnabled(true);
        // this is the important code :)
        // Without it the view will have a dimension of 0,0 and the bitmap will be null
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.setDrawingCacheBackgroundColor(0xffff00);
        if (v.getWidth() <= 0 || v.getHeight() <= 0) {
            HSLog.d("ShareAlertActivity", "view measure failed");
            Exception exception = new Exception("ViewSizeInvalidException");
            return null;
        }
        HSLog.d("ShareAlertActivity", "max cache size = " + ViewConfiguration.get(this).getScaledMaximumDrawingCacheSize() + "");
        v.buildDrawingCache(true);

        if (v.getDrawingCache() == null) {
            Exception exception = new Exception("OutOfMaxCacheSizeException");
            return null;
        }
        HSLog.d("ShareAlertActivity", "drawing cache size = " + v.getDrawingCache().getByteCount() + "");
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); // clear drawing cache
        return b;
    }

    private void getBitmap(String url, int w, int h, @NonNull final BitmapFetcher bitmapFetcher) {
        GlideApp.with(this)
                .asBitmap()
                .centerCrop()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        bitmapFetcher.onResourceReady(resource);
                        return false;
                    }
                }).submit(w, h);
    }

    private void getActionViewBitmap(final View root, final BitmapFetcher bitmapFetcher) {
        getBitmap(themeType.getAcceptIcon(), 280, 280, new BitmapFetcher() {
            @Override
            public void onResourceReady(Bitmap resource) {
                ImageView acceptCall = root.findViewById(R.id.call_accept);
                acceptCall.setImageBitmap(resource);
                getBitmap(themeType.getRejectIcon(), 280, 280, new BitmapFetcher() {
                    @Override
                    public void onResourceReady(Bitmap resource) {
                        ImageView rejectCall = root.findViewById(R.id.call_reject);
                        rejectCall.setImageBitmap(resource);
                        bitmapFetcher.onResourceReady(resource);
                    }
                });
            }
        });
    }

    private int getLayout() {
        switch (themeType.getValue()) {
            case Type.LED:
                return R.layout.theme_led_preview_for_share;
            case Type.TECH:
                return R.layout.theme_tech_preview_for_share;
            default:
                return R.layout.share_view_layout;
        }
    }

    private File getTempShareDirectory(Context context) {
        File file = new File(context.getExternalFilesDir(null), "Share");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
