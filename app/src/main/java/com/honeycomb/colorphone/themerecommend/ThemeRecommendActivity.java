package com.honeycomb.colorphone.themerecommend;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.acb.utils.Utils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ThemeRecommendActivity extends HSAppCompatActivity {
    private View mNavBack;
    private Theme mTheme;
    private Type mThemeType;
    private ArrayList<Theme> mThemes = new ArrayList<>();

    private ThemePreviewWindow mPreview;
    private InCallActionView mCallActionView;
    private TextView mContent;
    private View mAction;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_recommend);
        mNavBack = findViewById(R.id.close_button);

        String phoneNumber = "13800138000";

        mContent = findViewById(R.id.recommend_view_content);
        mAction = findViewById(R.id.recommend_view_action);

        mPreview = findViewById(R.id.prev_flash_window);
        mPreview.setPhoneNumber(phoneNumber);
        mPreview.setPreviewType(ThemePreviewWindow.PreviewType.GUIDE);

        mCallActionView = findViewById(R.id.in_call_view);
        mCallActionView.enableFullScreen(true);

        show(phoneNumber);
    }

    public void show(String number) {
        final String phoneNumber = number;
        if (!TextUtils.isEmpty(number)) {
            int themeID = ScreenFlashManager.getInstance().getAcbCallFactory().getIncomingReceiverConfig().getThemeIdByPhoneNumber(number);
            mThemeType = Utils.getTypeByThemeId(themeID);
            mPreview.playAnimation(mThemeType);
            mCallActionView.setTheme(mThemeType);
            // onStartCommand may called twice, Odd!
//            if (!isReadingContact) {
//                isReadingContact = true;
//                Executors.newSingleThreadExecutor()
//                        .execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                loadContactInfoBackground(phoneNumber);
//                            }
//                        });
//            }
        }
    }

    protected List<Theme> getThemes() {
        return Theme.themes();
    }
}
