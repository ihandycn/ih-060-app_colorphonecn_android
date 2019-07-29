package com.honeycomb.colorphone.customize.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.OverlayInstaller;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.theme.ThemeConstants;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.CustomizeContentView;
import com.honeycomb.colorphone.customize.view.LayoutWrapper;
import com.honeycomb.colorphone.customize.view.OnlineWallpaperPage;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.HomeKeyWatcher;

import net.appcloudbox.AcbAds;
import net.appcloudbox.UnreleasedAdWatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main activity for customize center.
 */
public class CustomizeActivity extends BaseCustomizeActivity
        implements INotificationObserver, OverlayInstaller, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String SCREEN_INTERACTIVE = "screen_interactivie";
    public static final String INTENT_EXTRA_SHOULD_SHOW_INTERSTITIAL_AD = "intent_extra_should_show_interstitial_ad";
    public static final String INTENT_EXTRA_IS_AUTO_START = "intent_extra_is_auto_start";

    public static final int REQUEST_CODE_SYSTEM_THEME_ALERT = 1;
    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;
    public static final int REQUEST_CODE_APPLY_3D_WALLPAPER = 3;

    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE = "notification_customize_activity_onpause";
    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME = "notification_customize_activity_onresume";
    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY = "notification_customize_activity_onDestroy";
    public static final String PREF_KEY_WALLPAPER_LAUNCHED_FROM_SHORTCUT = "wallpaper_launched_from_shortcut";
    public static final String PREF_KEY_THEME_LAUNCHED_FROM_SHORTCUT = "theme_launched_from_shortcut";

    private static final SparseIntArray ITEMS_INDEX_MAP = new SparseIntArray(4);
    private static final SparseArray<String> ITEMS_FLURRY_NAME_MAP = new SparseArray<>(4);

    private Intent mIntent;

    private CustomizeContentView mContent;

    private List<ActivityResultHandler> mActivityResultHandlers = new ArrayList<>(1);
    private LayoutWrapper mLayoutWrapper;

    private OnlineThemeApkStateMonitor mThemeApkMonitor;

    private int mViewIndex;
    public int mThemeTabIndex;
    public int mWallpaperTabIndex;
    public String mWallpaperTabItemName;
    public String mOpenFromSrc = "";
    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mFirstCome;


    public static Intent getLaunchIntent(Context context, String flurryFrom, int tabSelected) {
        return getLaunchIntent(context, flurryFrom, tabSelected, false);
    }

    public static Intent getLaunchIntent(Context context, String flurryFrom, int tabSelected, boolean showInterstitialAd) {
        Intent intent = new Intent(context, CustomizeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM, flurryFrom);
        intent.putExtra(ThemeConstants.INTENT_KEY_TAB, tabSelected);
        intent.putExtra(INTENT_EXTRA_SHOULD_SHOW_INTERSTITIAL_AD, showInterstitialAd);
        if ("Auto start".equals(flurryFrom)) {
            intent.putExtra(INTENT_EXTRA_IS_AUTO_START, true);
        }
        return intent;
    }

    public static void bindScrollListener(Context context, RecyclerView recyclerView, boolean hasBottom) {
       // Nothing
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
        HSBundle bundle = new HSBundle();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHomeKeyWatcher = new HomeKeyWatcher(this);
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
            }

            @Override
            public void onRecentsPressed() {
            }
        });
        mHomeKeyWatcher.startWatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        AcbAds.getInstance().setActivity(this);
        closeUnreleasedAdWatcher();
        mContent = findViewById(R.id.customize_content);
//        mBottomBar = findViewById(R.id.bottom_bar);
        mWallpaperTabItemName = getIntent().getStringExtra(ThemeConstants.INTENT_KEY_WALLPAPER_TAB_ITEM_NAME);
        mIntent = getIntent();
        handleIntent(mIntent);
        mThemeTabIndex = getIntent().getIntExtra(ThemeConstants.INTENT_KEY_THEME_TAB, 0);
        OnlineWallpaperPage.TabsConfiguration galleryTabsConfig = new OnlineWallpaperPage.TabsConfiguration();
        mWallpaperTabIndex = getIntent().getIntExtra(ThemeConstants.INTENT_KEY_WALLPAPER_TAB, galleryTabsConfig.tabIndexHot);
        mOpenFromSrc = getIntent().getStringExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM);
        mThemeApkMonitor = new OnlineThemeApkStateMonitor();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        BroadcastCenter.register(this, mThemeApkMonitor, filter);

        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);
        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);

//        ApkInfo.restore(this);

    }

    private void closeUnreleasedAdWatcher() {
        if (BuildConfig.DEBUG) {
            UnreleasedAdWatcher.getInstance().setEnabled(false);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
        handleIntent(intent);
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
    }

    private void handleIntent(Intent intent) {
        selectTabFromIntent(intent);
        String from = intent.getStringExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM);

//        String openFrom = "Other";
//        if (mViewIndex == TAB_INDEX_THEME) {
//            openFrom = from;
//        } else if (mViewIndex == TAB_INDEX_WALLPAPER) {
//            if (TextUtils.isEmpty(from) && Intent.ACTION_MAIN.equals(intent.getAction())) {
//                openFrom = "Other";
//            } else {
//                openFrom = TextUtils.isEmpty(from) ? "Other" : from;
//            }
//        }
//        logOpenFrom(mViewIndex, openFrom);
    }

    private void logOpenFrom(int tabSelected, String openFromDescription) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        WallpaperMgr.getInstance().initLocalWallpapers(mService, null);
        mContent.onServiceConnected(mService);
        selectTabFromIntent(mIntent);
    }

    private int findBottomBarTypeIdByIndex(int index) {
        for (int i = 0; i < ITEMS_INDEX_MAP.size(); i++) {
            int id = ITEMS_INDEX_MAP.keyAt(i);
            if (ITEMS_INDEX_MAP.get(id) == index) {
                return id;
            }
        }

        return 0;
    }

    private void selectTabFromIntent(Intent intent) {
        int tabSelected = intent.getIntExtra(ThemeConstants.INTENT_KEY_TAB, 0);
        String from = intent.getStringExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM);

        if (mViewIndex == 0) {
            mFirstCome = true;
        }
        mViewIndex = tabSelected;
        mContent.setChildSelected(mViewIndex);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }

    private void clearWallpaperData() {
        mWallpaperTabIndex = 0;
        mWallpaperTabItemName = "";
    }

    private void clearThemeData() {
        mThemeTabIndex = 0;
    }


    @Override
    public void installOverlay(View overlay, FrameLayout.LayoutParams params) {
        // Overlay must be installed onto decor view and add status / nav bar inset manually. Adding to activity content
        // root will not work.
        ((ViewGroup) getWindow().getDecorView()).addView(overlay, params);
    }

    @Override
    public void uninstallOverlay(View overlay) {
        ((ViewGroup) getWindow().getDecorView()).removeView(overlay);
    }

    @Override
    public View getOverlayee() {
        // We install overlay onto decor view, but returns activity content root here to exclude status / nav bar
        return findViewById(android.R.id.content);
    }

    public void addActivityResultHandler(ActivityResultHandler handler) {
        Iterator<ActivityResultHandler> iter = mActivityResultHandlers.iterator();
        while (iter.hasNext()) {
            ActivityResultHandler registered = iter.next();
            if (registered.getClass().isInstance(handler)) {
                iter.remove();
            }
        }
        mActivityResultHandlers.add(handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            default:
                // Dispatch to handlers only if we do not consume the result
                for (ActivityResultHandler handler : mActivityResultHandlers) {
                    handler.handleActivityResult(this, requestCode, resultCode, data);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (quitEditingMode()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        quitEditingMode();
        mHomeKeyWatcher.stopWatch();
    }

    private boolean quitEditingMode() {
        for (int index = 0; index < mContent.getChildCount(); index++) {
            View view = mContent.getChildAt(index);

            if (view instanceof OnlineWallpaperPage && ((OnlineWallpaperPage) view).isShowingCategories()) {
                ((OnlineWallpaperPage) view).hideCategoriesView();
                return true;
            }
        }

        return false;
    }

    public void setWallpaperIndex(int index) {
        if (mContent == null) {
            return;
        }

        for (int i = 0; i < mContent.getChildCount(); i++) {
            View view = mContent.getChildAt(i);
            if (view instanceof OnlineWallpaperPage) {
                ((OnlineWallpaperPage) view).setIndex(index);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultHandlers.clear();
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);

        BroadcastCenter.unregister(this, mThemeApkMonitor);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY);
        HSGlobalNotificationCenter.removeObserver(this);

        for (int index = 0; index < mContent.getChildCount(); index++) {
            View view = mContent.getChildAt(index);
//            if (view instanceof OnlineThemePage) {
//                ((OnlineThemePage) view).onDestroy();
//            }
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (s.equals(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED) || s.equals(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER)) {
            // Refresh local wallpaper
        }
    }

    public interface ActivityResultHandler {
        void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data);
    }

    private class OnlineThemeApkStateMonitor implements BroadcastListener {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isOnlineTheme = CustomizeUtils.isThemePackage(intent.getData().getSchemeSpecificPart());
            if (!isOnlineTheme) {
                return;
            }
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_PACKAGE_REMOVED:
                case Intent.ACTION_PACKAGE_ADDED:
                    for (int index = 0; index < mContent.getChildCount(); index++) {
                        View view = mContent.getChildAt(index);
//                        if (view instanceof OnlineThemePage) {
//                            ((OnlineThemePage) view).refresh();
//                        }
                    }
//                    if (mContent != null && mContent.getLocalCustomizePage() != null) {
//                        mContent.getLocalCustomizePage().reloadLocalTheme();
//                    }
                    break;
                default:
            }
        }
    }
}
