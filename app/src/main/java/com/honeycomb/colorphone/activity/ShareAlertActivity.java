package com.honeycomb.colorphone.activity;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.constant.CPConst;
import com.acb.call.themes.Type;
import com.acb.call.views.CircleImageView;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.ShareReceiver;
import com.honeycomb.colorphone.util.ShareAlertAutoPilotUtils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class ShareAlertActivity extends Activity {

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

    private ThemePreviewWindow themePreviewWindow;
    private InCallActionView inCallActionView;

    private int themeID = HSPreferenceHelper.getDefault().getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, Type.LED);
    private Type themeType = com.acb.utils.Utils.getTypeByThemeId(themeID);
    private boolean isInsideApp;
    private boolean v22 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static void starInsideApp(Activity activity) {
        Intent intent = new Intent(activity, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_INSIDE_APP, true);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_IN_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_IN_APP_SHOW_COUNT);
    }

    public static void startOutsideApp(Context context, UserInfo userInfo) {
        Intent intent = new Intent(context, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(IS_INSIDE_APP, false);
        intent.putExtra(USER_INFO, userInfo);
        context.startActivity(intent);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_OUT_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_OUT_APP_SHOW_COUNT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        isInsideApp =  getIntent().getBooleanExtra(IS_INSIDE_APP, true);

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

        if (isInsideApp) {
            ShareAlertAutoPilotUtils.logInsideAppShareAlertShow();
            HSAnalytics.logEvent("Colorphone_Inapp_ShareAlert_Show", "themeName", themeType.getName(), "v22", String.valueOf(v22));
        } else {
            ShareAlertAutoPilotUtils.logOutsideAppShareAlertShow();
            HSAnalytics.logEvent("Colorphone_Outapp_ShareAlert_Show", "themeName", themeType.getName(), "v22", String.valueOf(v22));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        themePreviewWindow.playAnimation(themeType);
        inCallActionView.setTheme(themeType);
        if (themePreviewWindow.getImageCover() != null) {
            themePreviewWindow.getImageCover().setVisibility(View.VISIBLE);
            setImageCover();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        themePreviewWindow.stopAnimations();
        inCallActionView.stopAnimations();
    }

    private void initThemePreviewWindow() {
        Intent intent = getIntent();
        UserInfo userInfo = (UserInfo) intent.getSerializableExtra(USER_INFO);

        themePreviewWindow = findViewById(R.id.card_flash_preview_window);
        inCallActionView = findViewById(R.id.card_in_call_action_view);
        inCallActionView.setEnabled(false);

        CircleImageView portrait = themePreviewWindow.findViewById(com.acb.call.R.id.caller_avatar);
        if (!isInsideApp) {
            TextView firstLineTextView = themePreviewWindow.findViewById(com.acb.call.R.id.caller_name);
            TextView secondLineTextView = themePreviewWindow.findViewById(com.acb.call.R.id.caller_number);
            if (userInfo.getPhotoUri() != null) {
                portrait.setImageURI(Uri.parse(userInfo.getPhotoUri()));
            } else {
                portrait.setVisibility(View.GONE);
            }
            if (userInfo.getCallName() != null) {
                firstLineTextView.setText(userInfo.getCallName());
            }
            secondLineTextView.setText(userInfo.getPhoneNumber());
        } else {
            portrait.setVisibility(View.GONE);
        }
    }

    private void initShareAlertText() {
        TextView title = findViewById(R.id.title);
        TextView content = findViewById(R.id.content);
        if (isInsideApp) {
            title.setText(ShareAlertAutoPilotUtils.getInsideAppShareAlertTitle());
            content.setText(ShareAlertAutoPilotUtils.getInsideAppShareDetail());
        } else {
            title.setText(ShareAlertAutoPilotUtils.getOutsideAppShareAlertTitle());
            content.setText(ShareAlertAutoPilotUtils.getOutsideAppShareDetail());
        }
    }

    private void initShareButton() {
        TextView shareButton = findViewById(R.id.share_button);
        shareButton.setText(isInsideApp ? ShareAlertAutoPilotUtils.getInsideAppShareBtnText() : ShareAlertAutoPilotUtils.getOutsideAppShareBtnText());
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
                if (isInsideApp) {
                    ShareAlertAutoPilotUtils.logInsideAppShareAlertClicked();
                    HSAnalytics.logEvent("Colorphone_Inapp_ShareAlert_Clicked", "themeName", themeType.getName(), "v22", String.valueOf(v22));
                } else {
                    ShareAlertAutoPilotUtils.logOutsideAppShareAlertClicked();
                    HSAnalytics.logEvent("Colorphone_Outapp_ShareAlert_Clicked", "themeName", themeType.getName(), "v22", String.valueOf(v22));
                }
            }
        });
    }

    private void share() {
        ViewGroup cardView = findViewById(R.id.card_view);
        cardView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        cardView.setDrawingCacheEnabled(true);
        cardView.buildDrawingCache(false);
        Bitmap bitmap = Bitmap.createBitmap(cardView.getDrawingCache());
        cardView.setDrawingCacheEnabled(false);
        Canvas c = new Canvas(bitmap);
        File file, f;
        try {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                file = new File(android.os.Environment.getExternalStorageDirectory(), "ColorPhone");
                if (!file.exists()) {
                    file.mkdirs();
                }
                f = new File(file.getAbsolutePath() + "/" + themeType.getName() + ".jpg");
                FileOutputStream ostream = new FileOutputStream(f);
                c.drawBitmap(bitmap, 0, 0, null);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();

                File sharefile = new File(file.getAbsolutePath() + "/" + themeType.getName() + ".jpg");
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharefile));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent receiver = new Intent(this, ShareReceiver.class);
                    receiver.putExtra(IS_INSIDE_APP, isInsideApp);
                    receiver.putExtra(ShareReceiver.THEME_NAME, themeType.getName());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent chooser = Intent.createChooser(share, "test", pendingIntent.getIntentSender());
                    startActivity(chooser);
                } else {
                    Intent chooser = Intent.createChooser(share, "test");
                    startActivity(chooser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    private void setImageCover(final View root) {
         GlideApp.with(this)
                .asBitmap()
                .centerCrop()
                .load(themeType.getPreviewImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                 .listener(new RequestListener<Bitmap>() {
                     @Override
                     public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                         return false;
                     }

                     @Override
                     public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                         ImageView imageView = root.findViewById(R.id.preview_image);
                         TextView name = root.findViewById(R.id.caller_name);
                         TextView number = root.findViewById(R.id.caller_number);
                         name.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
                         number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                         imageView.setImageBitmap(resource);

                         InCallActionView inCall = root.findViewById(R.id.in_call_view);
                         inCall.setTheme(themeType);

                         new Handler().postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 share(root);
                             }
                         }, 2000);

                         return false;
                     }
                 })
                .submit(Utils.getPhoneWidth(this), Utils.getPhoneHeight(this));
    }

    private Bitmap getScreenViewBitmap(View v) {
        v.setDrawingCacheEnabled(true);
        // this is the important code :)
        // Without it the view will have a dimension of 0,0 and the bitmap will be null
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); // clear drawing cache
        return b;
    }
}
