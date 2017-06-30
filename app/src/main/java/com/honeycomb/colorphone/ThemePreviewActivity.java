package com.honeycomb.colorphone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.honeycomb.colorphone.view.FlickerProgressBar;

/**
 * Created by sundxing on 17/6/29.
 */

public class ThemePreviewActivity extends AppCompatActivity {

    private ThemePreviewWindow previewWindow;
    private InCallActionView callActionView;
    private FlickerProgressBar progressBtn;
    private ImageView previewImage;
    private Theme mTheme;


    public static void start(Context context, Theme theme) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("theme", theme);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheme = (Theme) getIntent().getSerializableExtra("theme");

        setContentView(R.layout.activity_theme_preview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        progressBtn = (FlickerProgressBar)findViewById(R.id.theme_progress_btn);
        previewWindow = (ThemePreviewWindow) findViewById(R.id.flash_view);
        callActionView = (InCallActionView) findViewById(R.id.in_call_view);
        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
        if (mTheme !=  null) {
            Type[] types = Type.values();
            if (types.length > mTheme.getThemeId()) {
                previewWindow.playAnimation(types[mTheme.getThemeId()]);
            }
            if (mTheme.getImageRes() > 0) {
                previewImage.setImageResource(mTheme.getImageRes());
            }

        }
        findViewById(R.id.nav_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        FlickerProgressBar.HintText hintText = new FlickerProgressBar.HintText();
        hintText.inProgress = getString(R.string.theme_download_progress);
        hintText.paused = getString(R.string.theme_download_resume);
        hintText.finished = getString(R.string.theme_apply);
        progressBtn.setHintText(hintText);
        progressBtn.setAlpha(0.8f);
        progressBtn.setProgress(60);
    }

    @Override
    protected void onStart() {
        super.onStart();
        previewWindow.startAnimations();
        callActionView.doAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        callActionView.stopAnimations();
        previewWindow.stopAnimations();

    }
}
