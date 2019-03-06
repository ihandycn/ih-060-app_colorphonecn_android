package com.honeycomb.colorphone.themerecommend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.acb.call.views.CircleImageView;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.acb.utils.Utils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.ArrayList;
import java.util.List;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class ThemeRecommendActivity extends AppCompatActivity {

    public static final String PHONE_NUMBER = "phone_number";
    public static final String THEME_ID_NAME = "theme_id_name";

    private View mNavBack;
    private Type mThemeType;
    private ArrayList<Theme> mThemes = new ArrayList<>();

    private ThemePreviewWindow mPreview;
    private InCallActionView mCallActionView;
    private TextView mContent;
    private TextView mAction;
    private ShareAlertActivity.UserInfo userInfo;

    public static void start(Context context, String phoneNumber, String themeIdName) {
        Intent intent = new Intent(context, ThemeRecommendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.putExtra(THEME_ID_NAME, themeIdName);
        context.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(FEATURE_NO_TITLE);
        getWindow().addFlags(FLAG_DISMISS_KEYGUARD | FLAG_FULLSCREEN);

        setContentView(R.layout.activity_theme_recommend);

        String number = getIntent().getStringExtra(PHONE_NUMBER);
        if (TextUtils.isEmpty(number)) {
            ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
            finish();
            return;
        }

        mNavBack = findViewById(R.id.close_button);
        mNavBack.setOnClickListener(v -> finish());

        SimpleContact sc = ContactManager.getInstance().getContact(number);

        mContent = findViewById(R.id.recommend_view_content);
        mAction = findViewById(R.id.recommend_view_action);
        mAction.setOnClickListener(v -> {
            if (mThemeType != null) {
                ContactManager.getInstance().setThemeIdByNumber(number, mThemeType.getId());
                ThemeRecommendManager.getInstance().putAppliedTheme(number, mThemeType.getIdName());
                ResultPageActivity.startForThemeRecommend(this, mThemeType);
                mPreview.stopAnimations();

                ThemeRecommendManager.logThemeRecommendClick();
                finish();
            }
        });

        if (sc == null) {
            mAction.setText(R.string.theme_set_for_all);
        }

        mPreview = findViewById(R.id.prev_flash_window);
        mPreview.setPhoneNumber(number);

        mCallActionView = findViewById(R.id.in_call_view);
        mCallActionView.enableFullScreen(true);

        show(number);
    }

    public void show(String number) {
        if (!TextUtils.isEmpty(number)){
            String themeIdName = getIntent().getStringExtra(THEME_ID_NAME);
            if (TextUtils.isEmpty(themeIdName)) {
                ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
                finish();
                return;
            }
            mThemeType = Utils.getTypeByThemeIdName(themeIdName);
            if (mThemeType == null) {
                ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
                finish();
                return;
            }

            mPreview.updateThemeLayout(mThemeType);
            mPreview.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);

            mPreview.playAnimation(mThemeType);
            mCallActionView.setTheme(mThemeType);

            userInfo = ModuleUtils.createUserInfoByPhoneNumber(this, number);
            if (userInfo == null) {
                userInfo = new ShareAlertActivity.UserInfo(number, "", "");
            }

            mContent.setText(String.format(getString(R.string.theme_recommend_content),
                    TextUtils.isEmpty(userInfo.getCallName())
                            ? getString(R.string.theme_recommend_content_default)
                            : userInfo.getCallName()));
            editUserView(mPreview);

            if (ThemeRecommendManager.isThemeRecommendAdShow() && ThemeRecommendManager.isThemeRecommendAdShowBeforeRecommend()) {
                ThemeRecommendManager.logThemeRecommendWireOnRecommendShouldShow();
                AcbInterstitialAd ad = ResultPageManager.getInstance().getInterstitialAd();
                if (ad != null) {
                    Threads.postOnMainThreadDelayed(() -> {
                        ThemeRecommendManager.logThemeRecommendWireOnRecommendShow();
                        ad.show();
                    }, 300);
                } else {
                    ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
                    finish();
                    return;
                }
            }

            ThemeRecommendManager.logThemeRecommendShow(number);
        }
    }

    private void editUserView(View root) {
        CircleImageView portrait = root.findViewById(com.acb.call.R.id.caller_avatar);
        TextView firstLineTextView = root.findViewById(R.id.first_line);
        TextView secondLineTextView = root.findViewById(R.id.second_line);

        float shadowOffset = com.acb.utils.Utils.pxFromDp(2);
        firstLineTextView.setShadowLayer(shadowOffset, 0, shadowOffset, Color.BLACK);
        secondLineTextView.setShadowLayer(shadowOffset * 0.5f, 0, shadowOffset * 0.7f, Color.BLACK);

        firstLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getBondFont());
        secondLineTextView.setTypeface(ScreenFlashManager.getInstance().getAcbCallFactory().getViewConfig().getNormalFont());

        if (userInfo == null) {
            firstLineTextView.setText(R.string.share_default_name);
            secondLineTextView.setText(R.string.share_default_number);
            setPortraitViewGone(portrait, root);
            return;
        }

        if (userInfo.getPhoneNumber() != null) {
            if (!TextUtils.isEmpty(userInfo.getPhotoUri())) {
                portrait.setImageURI(Uri.parse(userInfo.getPhotoUri()));
            } else {
                setPortraitViewGone(portrait, root);
            }

            if (!TextUtils.isEmpty(userInfo.getCallName())) {
                firstLineTextView.setText(userInfo.getCallName());
            }
            secondLineTextView.setText(PhoneNumberUtils.formatNumber(userInfo.getPhoneNumber()));
        } else {
            setPortraitViewGone(portrait, root);
        }
    }

    private void setPortraitViewGone(ImageView portrait, View root) {
        portrait.setVisibility(View.GONE);
        if (mThemeType.getValue() == Type.TECH) {
            root.findViewById(R.id.caller_avatar_container).setVisibility(View.GONE);
        }
    }

    protected List<Theme> getThemes() {
        return Theme.themes();
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        if (ThemeRecommendManager.isThemeRecommendAdShow() && ThemeRecommendManager.isThemeRecommendAdShowBeforeRecommend()) {
            ResultPageManager.getInstance().releaseInterstitialAd();
        }
    }

    @Override protected void onStart() {
        super.onStart();
        AutopilotEvent.onExtendedActive();
    }

    @Override protected void onStop() {
        super.onStop();
        AutopilotEvent.onExtendedDeactive();
    }
}
