package com.honeycomb.colorphone.themerecommend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class ThemeRecommendActivity extends HSAppCompatActivity {

    public static final String PHONE_NUMBER = "phone_number";

    private View mNavBack;
    private Type mThemeType;
    private ArrayList<Theme> mThemes = new ArrayList<>();

    private ThemePreviewWindow mPreview;
    private InCallActionView mCallActionView;
    private TextView mContent;
    private View mAction;
    private ShareAlertActivity.UserInfo userInfo;

    public static void start(Context context, String phoneNum) {
        Intent intent = new Intent(context, ThemeRecommendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(PHONE_NUMBER, phoneNum);
        context.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResultPageManager.getInstance().preloadResultPageAds();

        requestWindowFeature(FEATURE_NO_TITLE);
        getWindow().addFlags(FLAG_DISMISS_KEYGUARD | FLAG_FULLSCREEN);

        setContentView(R.layout.activity_theme_recommend);

        String number = (String) getIntent().getSerializableExtra(PHONE_NUMBER);
        if (TextUtils.isEmpty(number)) {
            finish();
            return;
        }

        mNavBack = findViewById(R.id.close_button);
        mNavBack.setOnClickListener(v -> finish());

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

        mPreview = findViewById(R.id.prev_flash_window);
        mPreview.setPhoneNumber(number);

        mCallActionView = findViewById(R.id.in_call_view);
        mCallActionView.enableFullScreen(true);

        show(number);
    }

    public void show(String number) {
        if (!TextUtils.isEmpty(number)){
            String themeIdName = ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(number);
            if (TextUtils.isEmpty(themeIdName)) {
                finish();
                return;
            }
            mThemeType = Utils.getTypeByThemeIdName(themeIdName);
            if (mThemeType == null) {
                if (BuildConfig.DEBUG) {
                    int themeID = ScreenFlashManager.getInstance().getAcbCallFactory().getIncomingReceiverConfig().getThemeIdByPhoneNumber(number);
                    mThemeType = Utils.getTypeByThemeId(themeID);
                } else {
                    finish();
                    return;
                }
            }

            mPreview.updateThemeLayout(mThemeType);
            mPreview.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);

            mPreview.playAnimation(mThemeType);
            mCallActionView.setTheme(mThemeType);

            userInfo = ModuleUtils.createUserInfoByPhoneNumber(this, number);
            if (userInfo == null) {
                finish();
                return;
            }

            mContent.setText(String.format(getString(R.string.theme_recommend_content),
                    TextUtils.isEmpty(userInfo.getCallName())
                            ? getString(R.string.theme_recommend_content_default)
                            : userInfo.getCallName()));
            editUserView(mPreview);

            ThemeRecommendManager.getInstance().recordThemeRecommendShow(number);
            ThemeRecommendManager.logThemeRecommendShow();
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
}
