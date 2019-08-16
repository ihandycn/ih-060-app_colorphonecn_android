package com.honeycomb.colorphone.startguide;

import android.animation.Animator;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.lock.AnimatorListenerAdapter;
import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuideItemHolder {
    private static final String TAG = StartGuideItemHolder.class.getSimpleName();

    public static final int PERMISSION_STATUS_HIDE = -1;
    public static final int PERMISSION_STATUS_NOT_START = 0;
    public static final int PERMISSION_STATUS_LOADING = 1;
    public static final int PERMISSION_STATUS_FAILED = 2;
    public static final int PERMISSION_STATUS_FIX = 3;
    public static final int PERMISSION_STATUS_OK = 4;

    @IntDef({PERMISSION_STATUS_HIDE,
            PERMISSION_STATUS_NOT_START,
            PERMISSION_STATUS_LOADING,
            PERMISSION_STATUS_OK,
            PERMISSION_STATUS_FAILED,
            PERMISSION_STATUS_FIX
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface PERMISSION_STATUS {
    }

    @StartGuidePermissionFactory.PERMISSION_TYPES
    int permissionType;

    boolean isConfirmPage = false;
    TextView text;
    ImageView ok;
    View fix;
    LottieAnimationView loading;
    boolean clickToFix = false;

    StartGuideItemHolder(View item, @StartGuidePermissionFactory.PERMISSION_TYPES int type, boolean isConfirm) {
        permissionType = type;
        isConfirmPage = isConfirm;

        text = item.findViewById(R.id.start_guide_permission_item_title);
        text.setText(StartGuidePermissionFactory.getItemTitle(type));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            text.setCompoundDrawablesRelativeWithIntrinsicBounds(StartGuidePermissionFactory.getItemDrawable(type), 0, 0, 0);
        } else {
            text.setCompoundDrawables(item.getContext().getResources().getDrawable(StartGuidePermissionFactory.getItemDrawable(type)), null, null, null);
        }

        ok = item.findViewById(R.id.start_guide_permission_auto_start_ok);
        ok.setTag(type);

        loading = item.findViewById(R.id.start_guide_permission_item_loading);

        fix = item.findViewById(R.id.start_guide_permission_auto_start_fix);
        fix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
        fix.setOnClickListener(v -> {
            clickToFix = true;
            if (fix.getContext() instanceof Activity) {
                StartGuidePermissionFactory.fixPermission(permissionType, (Activity) fix.getContext());
            } else {
                StartGuidePermissionFactory.fixPermission(permissionType, null);
            }
        });

        if (!isConfirmPage) {
            checkGrantStatus();
        }
    }

    boolean checkGrantStatus() {
        boolean grant = StartGuidePermissionFactory.getItemGrant(permissionType);
        setStatus(grant ? PERMISSION_STATUS_OK : (isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_LOADING));
        return grant;
    }

    int getStatus() {
        return Integer.valueOf(ok.getTag().toString());
    }

    void setStatus(@PERMISSION_STATUS int status) {
        int lastStatus = getStatus();
        switch (status) {
            case PERMISSION_STATUS_FAILED:
                if (lastStatus == PERMISSION_STATUS_LOADING) {
                    loading.addAnimatorListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                            loading.cancelAnimation();
                            loading.removeAllAnimatorListeners();
                            loading.setVisibility(View.INVISIBLE);

                            ok.setVisibility(View.VISIBLE);
                            ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
//                            setPermissionStatus(permissionType + 1, PERMISSION_STATUS_LOADING);
                        }
                    });
                } else {
                    loading.setVisibility(View.INVISIBLE);
                    ok.setVisibility(View.VISIBLE);
                    ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                }
                break;
            case PERMISSION_STATUS_FIX:
                if (fix != null) {
                    ok.setVisibility(View.INVISIBLE);
                    fix.setVisibility(View.VISIBLE);

                    ok.animate().alpha(0.7f).setDuration(100).start();
                    text.animate().alpha(0.6f).setDuration(100).start();
                }
                break;
            case PERMISSION_STATUS_LOADING:
                if (lastStatus != PERMISSION_STATUS_OK) {
                    if (loading != null) {
                        ok.setVisibility(View.INVISIBLE);
                        loading.setVisibility(View.VISIBLE);
                        loading.useHardwareAcceleration();
                        loading.playAnimation();
                    }
                }
                if (fix != null) {
                    fix.setVisibility(View.INVISIBLE);
                }
                break;
            case PERMISSION_STATUS_NOT_START:
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                if (loading != null) {
                    loading.setVisibility(View.INVISIBLE);
                }
                break;
            case PERMISSION_STATUS_OK:
                if (lastStatus == PERMISSION_STATUS_LOADING) {
                    ok.setVisibility(View.INVISIBLE);
                    loading.addAnimatorListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                            loading.cancelAnimation();
                            loading.setAnimation("lottie/start_guide/permission_done.json");
                            loading.setRepeatCount(0);
                            loading.playAnimation();
                            HSLog.i(TAG, "onAnimationRepeat " + permissionType);

                            loading.removeAllAnimatorListeners();
                            loading.addAnimatorListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    ok.setVisibility(View.VISIBLE);
                                    ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                                    loading.removeAllAnimatorListeners();
                                    loading.setVisibility(View.INVISIBLE);
//                                    setPermissionStatus(permissionType + 1, PERMISSION_STATUS_LOADING);
                                    HSLog.i(TAG, "onAnimationEnd " + permissionType);

                                    ok.animate().alpha(0.3f).setDuration(100).start();
                                    text.animate().alpha(0.3f).setDuration(100).start();
                                }
                            });
                        }
                    });
                } else {
                    if (loading != null) {
                        loading.cancelAnimation();
                        loading.setAnimation("lottie/start_guide/permission_done.json");
                        loading.setRepeatCount(0);
                        loading.playAnimation();
                        HSLog.i(TAG, "only done animation " + permissionType);

                        loading.removeAllAnimatorListeners();
                        loading.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ok.setVisibility(View.VISIBLE);
                                ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                                loading.removeAllAnimatorListeners();
                                loading.setVisibility(View.INVISIBLE);
//                                setPermissionStatus(permissionType + 1, PERMISSION_STATUS_LOADING);
                                HSLog.i(TAG, "onAnimationEnd " + permissionType);

                                ok.animate().alpha(0.3f).setDuration(100).start();
                                text.animate().alpha(0.3f).setDuration(100).start();
                            }
                        });
                    } else {
                        ok.setVisibility(View.VISIBLE);
                        ok.setImageResource(R.drawable.start_guide_confirm_ok_image);

                        ok.animate().alpha(0.3f).setDuration(100).start();
                        text.animate().alpha(0.3f).setDuration(100).start();
                    }
                }

                if (fix != null) {
                    fix.setVisibility(View.INVISIBLE);
                }

                break;
            case PERMISSION_STATUS_HIDE:
                break;
        }
        ok.setTag(status);
    }
}
