package com.honeycomb.colorphone.wallpaper.livewallpaper.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.honeycomb.colorphone.GLParams;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.dialog.FloatWindowDialog;
import com.honeycomb.colorphone.wallpaper.dialog.FloatWindowManager;
import com.honeycomb.colorphone.wallpaper.dialog.SafeWindowManager;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

/**
 * Created by sundxing on 2018/5/26.
 */

public class WallpaperTestWindow extends FloatWindowDialog {

    private int mType;

    private static SparseArray<WallpaperTestWindow> sGuideMap = new SparseArray<>();

    public static void hide(int type) {
        if (sGuideMap.get(type) != null) {
            sGuideMap.get(type).dismiss();
        }
    }

    public static void show(Context context, int type) {
        hide(type);

        if (BuildConfig.DEBUG) {
            WallpaperTestWindow guide = new WallpaperTestWindow(context, type);
            sGuideMap.put(type, guide);
            FloatWindowManager.getInstance().showDialog(guide);
        }
    }

    public WallpaperTestWindow(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.wallpaper_test_params_input,this);
        v.setVisibility(INVISIBLE);
        v.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                die();
            }
        });

        EditText eyeEdit = v.findViewById(R.id.image1);
        eyeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.gravityConFactors[0] = value;
                HSLog.d("SUNDXING", "image1 = " + value);
            }
        });
        EditText aspectEdit = v.findViewById(R.id.image2);
        aspectEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.gravityConFactors[1] = value;
                HSLog.d("SUNDXING", "image1 = " + value);
            }
        });
        EditText nearEdit = v.findViewById(R.id.image3);
        nearEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.gravityConFactors[2] = value;
                HSLog.d("SUNDXING", "nearEdit = " + value);
            }
        });

        EditText image4 = v.findViewById(R.id.image4);
        image4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.gravityConFactors[3] = value;
                HSLog.d("SUNDXING", "image4 = " + value);
            }
        });

        EditText offset = v.findViewById(R.id.offset);
        offset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.offsetRatio = value;
                HSLog.d("SUNDXING", "offsetRatio = " + value);
            }
        });

        EditText fovy = v.findViewById(R.id.fovy);
        fovy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.fovy = value;
                HSLog.d("SUNDXING", "fovy = " + value);
            }
        });

        EditText rotate_factor = v.findViewById(R.id.rotate_factor);
        rotate_factor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.rotateFactor = value;
                HSLog.d("SUNDXING", "fovy = " + value);
            }
        });

        EditText maxDegree = v.findViewById(R.id.rotate_max_degree);
        maxDegree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = formatValueSafely(s);
                GLParams.rotateMaxDegree = value;
                HSLog.d("SUNDXING", "fovy = " + value);
            }
        });
    }

    private float formatValueSafely(Editable s) {
        String v = s.toString().trim();
        if (TextUtils.isEmpty(v)) {
            return 0f;
        }
        return Float.valueOf(v);
    }

    @Override
    protected String getGroupTag() {
        return LiveWallpaperConsts.GUIDE_WINDOW_TAG;
    }

    @Override
    public void dismiss() {
        sGuideMap.remove(mType);
        this.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(INVISIBLE);
                FloatWindowManager.getInstance().removeDialog(WallpaperTestWindow.this);
            }
        }).start();
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sGuideMap.remove(mType);
    }

    /**
     * No need to show this type of guide ever again.
     */
    public void die() {
        dismiss();
    }


    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = Dimensions.pxFromDp(72);
        lp.gravity = Gravity.BOTTOM;
        lp.format = PixelFormat.RGBA_8888;
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        return lp;
    }

    @Override
    public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        setVisibility(VISIBLE);
        setAlpha(0.1f);
        animate().alpha(1).setDuration(200).start();
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }


}
