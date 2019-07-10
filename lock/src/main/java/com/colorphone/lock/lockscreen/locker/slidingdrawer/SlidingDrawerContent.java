package com.colorphone.lock.lockscreen.locker.slidingdrawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.lock.boost.RamUsageDisplayUpdater;
import com.colorphone.lock.lockscreen.SystemSettingsManager;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.wallpaper.WallpaperContainer;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.flashlight.FlashlightManager;
import com.superapps.util.Bitmaps;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.colorphone.lock.lockscreen.SystemSettingsManager.SettingsItem.BRIGHTNESS;
import static com.colorphone.lock.lockscreen.SystemSettingsManager.SettingsItem.RINGMODE;
import static com.colorphone.lock.lockscreen.SystemSettingsManager.SettingsItem.WIFI;


public class SlidingDrawerContent extends FrameLayout
        implements View.OnClickListener,
        SystemSettingsManager.ISystemSettingsListener, SeekBar.OnSeekBarChangeListener,
        INotificationObserver, RamUsageDisplayUpdater.RamUsageChangeListener {

    public final static String EVENT_SHOW_BLACK_HOLE = "EVENT_SHOW_BLACK_HOLE";
    public static final String EVENT_BLACK_HOLE_ANIMATION_END = "EVENT_BLACK_HOLE_ANIMATION_END";
    public static final String EVENT_REFRESH_BLUR_WALLPAPER = "EVENT_REFRESH_BLUR_WALLPAPER";
    public final static int DURATION_BALL_DISAPPEAR = 400;
    public final static int DURATION_BALL_APPEAR = 300;
    private static final int WALLPAPER_BLUR_RADIUS = 8;

    private static final int EVENT_SYSTEM_SETTING_WIFI = 100;
    private static final int EVENT_SYSTEM_SETTING_BLUETOOTH = 101;
    private static final int EVENT_SYSTEM_SETTING_SOUND_PROFILE = 102;
    private static final int EVENT_SYSTEM_SETTING_MOBILE_DATA = 103;
    private static final long SYSTEM_SETTING_CHECK_DELAY = 1500;
    private static final long SYSTEM_SETTING_CHECK_DELAY_LONG = 2000;

    private Locker mLocker;

    List<String> mCalculatorApps;
    private ImageView flashlight;
    private ImageView wifiState;
    private ImageView bluetoothState;
    private ImageView soundProfileState;
    private ImageView mobileDataState;
    private SeekBar brightnessBar;

    private BallAnimationView ballAnimationView;
    private View ballAnimationContainer;
    private TextView tvMemory;
    private int mBeforeBoostRamUsage;
    private int mAfterBoostRamUsage;
    private int mCurrentRamUsage;

    private int[] bluetoothStateRes;
    private int[] mobileDataStateRes;
    private int[] soundStateRes;
    private int[] wifiStateRes;
    private int brightnessValue;
    private boolean isCameraUsageAccess = true;
    private boolean isDrawerBgInitial = false;
    private Timer mMobileDataPollingTimer;
    private int mobileDataLastState = 0;

    private ImageView ivDrawerBg;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SYSTEM_SETTING_WIFI:
                    LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Wifi");
                    wifiState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_BLUETOOTH:
                    LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Bluetooth");
                    bluetoothState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_MOBILE_DATA:
                    mobileDataState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_SOUND_PROFILE:
                    soundProfileState.setEnabled(true);
                default:
                    break;
            }
        }
    };

    private SystemSettingsManager mSystemSettingsManager;

    public SlidingDrawerContent(Context context) {
        this(context, null);
    }

    public SlidingDrawerContent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawerContent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSystemSettingsManager = new SystemSettingsManager(getContext());
    }

    public void setLockScreen(Locker locker) {
        mLocker = locker;

        WallpaperContainer wallpaperContainer = (WallpaperContainer) findViewById(R.id.locker_wallpaper_container);
        wallpaperContainer.setLocker(locker);
    }

    public void setDrawerBg(final Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override public void run() {
                final Bitmap bluredBitmap = blurBitmap(getContext(), bitmap, WALLPAPER_BLUR_RADIUS);
                Threads.postOnMainThread(new Runnable() {
                    @Override public void run() {
                        /*ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(ivDrawerBg, "alpha", 1f, 0.5f);
                        wallpaperOut.setDuration(400);
                        wallpaperOut.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ivDrawerBg.setImageBitmap(bluredBitmap);
                            }
                        });

                        ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(ivDrawerBg, "alpha", 0.5f, 1f);
                        wallpaperIn.setDuration(400);

                        AnimatorSet change = new AnimatorSet();
                        change.playSequentially(wallpaperOut, wallpaperIn);
                        change.start();*/
                        ivDrawerBg.setImageBitmap(bluredBitmap);
                    }
                });
            }
        });
    }

    /**
     * Calculate the range of the toggle background, and return the blurred range.
     *
     * @return Blurred bitmap. Original bitmap for inputs with illegal dimensions.
     */
    static Bitmap blurBitmap(Context context, Bitmap bitmap, int radius) {
        // resize the bitmap to fit sliding drawer
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return bitmap;
        }
        int startX;
        int startY;
        int rangeHeight;

        int rangeWidth = height * Dimensions.getPhoneWidth(context) / Dimensions.getPhoneHeight(context);
        if (rangeWidth <= width) {
            rangeHeight = height * context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height)
                    / Dimensions.getPhoneHeight(context);
            startX = (width - rangeWidth) / 2;
            startY = height - rangeHeight;
        } else {
            rangeWidth = width;
            startX = 0;
            startY = (height + Dimensions.getPhoneHeight(context)) / 2
                    - context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height);
            rangeHeight = context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height);
        }
        if (Dimensions.hasNavBar(context)) {
            startY -= Dimensions.getNavigationBarHeight(context);
        }

        // Clamp the left & top of blurred area first
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;

        // If left / top of blurred area is calculated to be out-of-bounds,
        // the input bitmap must be of illegal dimensions that we cannot blur.
        if (startX >= width || startY >= height) {
            return bitmap;
        }

        // Clamp the right & bottom of blurred area
        if (startX + rangeWidth > width) rangeWidth = width - startX;
        if (startY + rangeHeight > height) rangeHeight = height - startY;

        // Guard against negative size of blurred output
        if (rangeWidth <= 0) rangeWidth = 1;
        if (rangeHeight <= 0) rangeHeight = 1;

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, startX, startY, rangeWidth, rangeHeight);

        // blur resized bitmap
        Bitmap blurSrcBitmap = Bitmap.createBitmap(Math.max(1, rangeWidth / 8), Math.max(1, rangeHeight / 8), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blurSrcBitmap);
        canvas.scale(1 / 8.0f, 1 / 8.0f);
        Paint paint = new Paint();
        paint.setFlags(2);
        canvas.drawBitmap(resizedBitmap, 0.0f, 0.0f, paint);
        try {
            return Bitmaps.fastBlur(blurSrcBitmap, 1, radius);
        } catch (Exception e) {
            return resizedBitmap;
        }
    }

    public void onScroll(float cur, float total) {
        if (ivDrawerBg != null) {
            ivDrawerBg.setTranslationY(-1 * cur / total * getMeasuredHeight());
        }
    }

    public void clearBlurredBackground() {
        if (ivDrawerBg != null) {
            ivDrawerBg.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ivDrawerBg = (ImageView) findViewById(R.id.iv_slide_bg);
        flashlight = (ImageView) findViewById(R.id.flashlight);
        ImageView calculator = (ImageView) findViewById(R.id.calculator);
        wifiState = (ImageView) findViewById(R.id.wifi);
        bluetoothState = (ImageView) findViewById(R.id.bluetooth);
        soundProfileState = (ImageView) findViewById(R.id.sound_profile);
        mobileDataState = (ImageView) findViewById(R.id.data);
        brightnessBar = (SeekBar) findViewById(R.id.brightness_seekbar);
        // pre-Lollipop progressBar tinting
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            Drawable wrapDrawable = DrawableCompat.wrap(brightnessBar.getProgressDrawable());
//            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getContext(), android.R.color.white));
//            brightnessBar.setProgressDrawable(DrawableCompat.unwrap(wrapDrawable));

            Drawable wrapDrawable = DrawableCompat.wrap(brightnessBar.getThumb());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getContext(), android.R.color.white));
            brightnessBar.setThumb(DrawableCompat.unwrap(wrapDrawable));
        }

        flashlight.setOnClickListener(this);
        flashlight.setImageResource(FlashlightManager.getInstance().isOn() ?
                R.drawable.locker_flashlight_on : R.drawable.locker_flashlight_off);
        calculator.setOnClickListener(this);
        wifiState.setOnClickListener(this);
        bluetoothState.setOnClickListener(this);
        soundProfileState.setOnClickListener(this);
        mobileDataState.setOnClickListener(this);
        brightnessBar.setProgress(mSystemSettingsManager.getSystemSettingsItemState(BRIGHTNESS));
        brightnessBar.setOnSeekBarChangeListener(this);

        tvMemory = (TextView) findViewById(R.id.txt_ball_memory);
        ballAnimationView = (BallAnimationView) findViewById(R.id.ball_animation);
        mCurrentRamUsage = mBeforeBoostRamUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
        tvMemory.setText(String.format("%d", mCurrentRamUsage));
        ballAnimationView.setProgress(mCurrentRamUsage);
        RamUsageDisplayUpdater.getInstance().startUpdatingRamUsage();
        RamUsageDisplayUpdater.getInstance().addRamUsageChangeListener(this);

        ballAnimationContainer = findViewById(R.id.ball_animation_container);
        ballAnimationContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator ballScale = ObjectAnimator.ofPropertyValuesHolder(ballAnimationContainer,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 0),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 0));
                ballScale.setInterpolator(new AnticipateInterpolator(2));
                ballScale.setDuration(DURATION_BALL_DISAPPEAR);
                ballScale.start();

//                mBeforeBoostRamUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();


                HSGlobalNotificationCenter.sendNotification(EVENT_SHOW_BLACK_HOLE);
                LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Boost");
            }
        });

        createToggleStateRes();

        Map<String, ?> appLists = HSConfig.getMap("Application", "AppLists");
        mCalculatorApps = (List<String>) appLists.get("Calculator");
        if (mCalculatorApps == null) mCalculatorApps = new ArrayList<>();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver(EVENT_BLACK_HOLE_ANIMATION_END, this);
        HSGlobalNotificationCenter.addObserver(EVENT_REFRESH_BLUR_WALLPAPER, this);

        mSystemSettingsManager.register(this);

        updateSystemToggles();

        postDelayed(new Runnable() {
            @Override public void run() {
                refreshDrawerBg();
            }
        }, 300);
    }

    @Override
    protected void onDetachedFromWindow() {
        HSGlobalNotificationCenter.removeObserver(this);
        mSystemSettingsManager.unRegister();

        if (mMobileDataPollingTimer != null) {
            mMobileDataPollingTimer.cancel();
        }
        mMobileDataPollingTimer = null;

        if (isCameraUsageAccess && !FlashlightManager.getInstance().isOn()) {
            FlashlightManager.getInstance().release();
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_BLACK_HOLE_ANIMATION_END:
                ObjectAnimator ballScale = ObjectAnimator.ofPropertyValuesHolder(ballAnimationContainer,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1));
                ballScale.setDuration(DURATION_BALL_APPEAR);
                ballScale.start();

                mAfterBoostRamUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
                for (int i = mBeforeBoostRamUsage; i >= mAfterBoostRamUsage; i--) {
                    final int k = i;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvMemory.setText(String.format("%d", k));
                            ballAnimationView.setProgress(k, mBeforeBoostRamUsage, mAfterBoostRamUsage);
                        }
                    }, (mBeforeBoostRamUsage - i) * 30 + DURATION_BALL_APPEAR);
                }
                HSLog.d("MemoryBoost", "boost before ram is " + mBeforeBoostRamUsage + " and after ram is " + mAfterBoostRamUsage);
                break;
            case EVENT_REFRESH_BLUR_WALLPAPER:
                refreshDrawerBg();
                break;
            default:
                break;
        }
    }

    private void refreshDrawerBg() {
        if (isDrawerBgInitial) return;
        isDrawerBgInitial = true;
        if (mLocker == null) {
            return;
        }
        Drawable currentWallpaper = mLocker.getIvLockerWallpaper().getDrawable();
        if (currentWallpaper != null && currentWallpaper instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) currentWallpaper).getBitmap();
            if (bitmap != null) {
                setDrawerBg(bitmap);
            } else {
                setDrawerBg(BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_locker));
            }
        } else {
            setDrawerBg(BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_locker));
        }
    }

    @Override
    public void onClick(View v) {
        Message msg = Message.obtain();
        int i = v.getId();
        if (i == R.id.wifi) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(WIFI);
            msg.what = EVENT_SYSTEM_SETTING_WIFI;
            handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            mSystemSettingsManager.toggleWifi();
            wifiState.setEnabled(false);

        } else if (i == R.id.bluetooth) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.BLUETOOTH);
            msg.what = EVENT_SYSTEM_SETTING_BLUETOOTH;
            handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            mSystemSettingsManager.toggleBluetooth();
            bluetoothState.setImageResource(bluetoothStateRes[3]);
            bluetoothState.setEnabled(false);

        } else if (i == R.id.sound_profile) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(RINGMODE);
            msg.what = EVENT_SYSTEM_SETTING_SOUND_PROFILE;
            handler.removeMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            try {
                mSystemSettingsManager.toggleRingMode();
            } catch (SecurityException e) {
            }

        } else if (i == R.id.data) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
            msg.what = EVENT_SYSTEM_SETTING_MOBILE_DATA;
            handler.removeMessages(EVENT_SYSTEM_SETTING_MOBILE_DATA);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY_LONG); // 比较耗时
            if (!SystemSettingsManager.setMobileDataStatus(getContext(),
                    mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA) == 1 ? false : true)) {
                SystemSettingsManager.startSystemDataUsageSetting(getContext());
                HSBundle bundle = new HSBundle();
                bundle.putString(Locker.EXTRA_DISMISS_REASON, "DataClick");
                HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
            } else {
                startMobileDataChecker();
            }

        } else if (i == R.id.flashlight) {
            if (FlashlightManager.getInstance().isOn()) {
                FlashlightManager.getInstance().turnOff();
                FlashlightManager.getInstance().release();
            } else {
                try {
                    FlashlightManager.getInstance().init();
                    FlashlightManager.getInstance().turnOn();
                } catch (Exception e) {
                    isCameraUsageAccess = false;
                }
            }
            LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Flashlight");
            flashlight.setImageResource(FlashlightManager.getInstance().isOn() ?
                    R.drawable.locker_flashlight_on : R.drawable.locker_flashlight_off);

        } else if (i == R.id.calculator) {
            if (startCalculator()) {
                HSBundle bundle = new HSBundle();
                bundle.putString(Locker.EXTRA_DISMISS_REASON, "CalcClick");
                HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
                openCalculator();
                LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Calculator");
            }
        } else {
        }
    }

    public void openCalculator(){
        PackageInfo pak = getAllApps(getContext(), "Calculator","calculator");
        if(pak != null){
            Intent intent = new Intent();
            intent = getContext().getPackageManager().getLaunchIntentForPackage(pak.packageName);
            getContext().startActivity(intent);
        }else{

        }
    }

    public  PackageInfo getAllApps(Context context,String app_flag_1,String app_flag_2) {
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> packlist = pManager.getInstalledPackages(0);
        for (int i = 0; i < packlist.size(); i++) {
            PackageInfo pak = (PackageInfo) packlist.get(i);
            if(pak.packageName.contains(app_flag_1)||pak.packageName.contains(app_flag_2)){
                return pak;
            }
        }
        return null;
    }

    private void startMobileDataChecker() {
        if (mMobileDataPollingTimer != null)
            return;
        mMobileDataPollingTimer = new Timer();
        mMobileDataPollingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int currentState = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
                        if (mobileDataLastState != currentState) {
                            Map<String, String> params = new HashMap<>();
                            params.put("type", "success");
                            if (currentState == 1) {
                                params.put("state", "on");
                                LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "MobileData");
                            } else {
                                params.put("state", "off");
                                LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "MobileData");
                            }
                            mobileDataLastState = currentState;
                        }
                        mobileDataState.setImageResource(mobileDataStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA)]);
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onSystemSettingsStateChanged(SystemSettingsManager.SettingsItem toggle, int state) {
        HSLog.i("MainFrame", "onSystemSettingStateChanged(), toggle = " + toggle + ", state = " + state);
        switch (toggle) {
            case BLUETOOTH:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_BLUETOOTH)) {
                    if (state == 1) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Bluetooth");
                        handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
                    } else if (state == 0) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Bluetooth");
                        handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
                    }
                }
                bluetoothState.setImageResource(bluetoothStateRes[state]);
                bluetoothState.setEnabled(true);
                break;
            case WIFI:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_WIFI)) {
                    Map<String, String> params = new HashMap<>();
                    if (state == 1) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Wifi");
                        handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
                    } else if (state == 0) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Wifi");
                        handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
                    }
                }
                wifiState.setImageResource(wifiStateRes[state]);
                wifiState.setEnabled(true);
                break;
            case BRIGHTNESS:
                int bright = mSystemSettingsManager.getSystemSettingsItemState(BRIGHTNESS);
                if (bright != brightnessValue) {
                    brightnessBar.setProgress(bright);
                }
                break;
            case RINGMODE:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE)) {
                    if (state == 2) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Sound");
                    } else if (state == 1) {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Sound");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Sound");
                    }
                    handler.removeMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE);
                }
                soundProfileState.setImageResource(soundStateRes[state]);
                soundProfileState.setEnabled(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mSystemSettingsManager.setSystemSettingsItemState(BRIGHTNESS, progress);
        }
        brightnessValue = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        brightnessValue = seekBar.getProgress();
        LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Switch_Clicked", "type", "Bright");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void createToggleStateRes() {
        TypedArray iconResArray = getContext().getResources().obtainTypedArray(R.array.bluetooth_state_drawable);
        bluetoothStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            bluetoothStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.mobile_data_state_drawable);
        mobileDataStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            mobileDataStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.sound_state_drawable);
        soundStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            soundStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.wifi_state_drawable);
        wifiStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            wifiStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();
    }

    private void updateSystemToggles() {
        wifiState.setImageResource(wifiStateRes[mSystemSettingsManager.getSystemSettingsItemState(WIFI)]);
        bluetoothState.setImageResource(bluetoothStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.BLUETOOTH)]);
        soundProfileState.setImageResource(soundStateRes[mSystemSettingsManager.getSystemSettingsItemState(RINGMODE)]);
        mobileDataState.setImageResource(mobileDataStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA)]);
        mobileDataLastState = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
    }

    private boolean startCalculator() {
        boolean result = true;
        try {
            openApp(mCalculatorApps);
        } catch (ActivityNotFoundException exception) {
            result = false;
        } catch (SecurityException e) {
            result = false;
        }
        return result;
    }

    private void openApp(List<String> candidateApps) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        // Code snippet from http://stackoverflow.com/questions/3590955/intent-to-launch-the-clock-application-on-android
        Intent openIntent = null;

        boolean foundImpl = false;

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < candidateApps.size(); i++) {
            String vendor = "Vendor " + i;
            String implName = candidateApps.get(i);
            String packageName;
            String className = "";

            if (implName.contains("/")) {
                packageName = implName.substring(0, implName.indexOf("/"));
                className = implName.substring(implName.indexOf("/") + 1);
                if (className.startsWith(".")) {
                    className = packageName + className;
                }
            } else {
                packageName = implName;
            }

            try {
                if (className.isEmpty()) {
                    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                    openIntent = packageManager.getLaunchIntentForPackage(packageName);
                } else {
                    ComponentName cn = new ComponentName(packageName, className);
                    packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA); // Throws when not found
                    openIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                    openIntent.setComponent(cn);
                }
                foundImpl = true;
                break;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        if (foundImpl && openIntent != null) {
            Navigations.startActivitySafely(getContext(), openIntent);
        }
    }

    @Override
    public void onDisplayedRamUsageChange(int displayedRamUsage) {
        mCurrentRamUsage = displayedRamUsage;
        tvMemory.setText(String.format("%d", mCurrentRamUsage));
        ballAnimationView.setProgress(mCurrentRamUsage);
    }

    @Override
    public void onBoostComplete(int afterBoostRamUsage) {

    }
}
