package com.acb.libwallpaper.live.customize.view;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acb.libwallpaper.live.animation.AnimatorListenerAdapter;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.animation.LauncherAnimUtils;
import com.acb.libwallpaper.live.customize.OverlayInstaller;
import com.acb.libwallpaper.live.customize.WallpaperInfo;
import com.acb.libwallpaper.live.customize.WallpaperMgr;
import com.acb.libwallpaper.live.customize.adapter.LocalWallpaperGalleryAdapter;
import com.acb.libwallpaper.live.customize.util.GridItemDecoration;
import com.acb.libwallpaper.live.util.Utils;
import com.acb.libwallpaper.live.view.RecyclerViewAnimator;
import com.acb.libwallpaper.live.view.recyclerview.SafeGridLayoutManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Preferences;

import java.util.List;

public class LocalCustomizePage extends LinearLayout implements View.OnClickListener {

    private static final String TAG = LocalCustomizePage.class.getSimpleName();

    private static final int TAB_INDEX_WALLPAPER = 0;

    private final OverlayInstaller mRoot;
    private final FrameLayout.LayoutParams mEditBarLp;
    private final int mEditBarHeight;
    private LocalCustomizePagerAdapter mAdapter;
    private ViewGroup mEditBar;
    private TextView mEditOkButton;
    private boolean mEditing;
    private PageEditListener mEditListener;

    public LocalCustomizePage(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = getResources();
        mRoot = (OverlayInstaller) context;
        mEditBarHeight = res.getDimensionPixelSize(R.dimen.bottom_bar_default_height);
        mEditBarLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mEditBarHeight, Gravity.BOTTOM);
        mEditBarLp.topMargin += Dimensions.getStatusBarHeight(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TabLayout tabs = ViewUtils.findViewById(this, R.id.local_customize_tabs);
        ViewPager viewPager = ViewUtils.findViewById(this, R.id.local_customize_pager);

        mAdapter = new LocalCustomizePagerAdapter(getContext());
        viewPager.setAdapter(mAdapter);
        tabs.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(Dimensions.isRtl() ? mAdapter.getCount() - 1 : 0, false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mEditing) {
                    exitEditMode(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        Utils.configTabLayoutText(tabs, Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD), 14f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabs.setElevation(getContext().getResources().getDimensionPixelOffset(R.dimen.app_bar_elevation));
        }
    }

    public void setPageEditListener(PageEditListener listener) {
        mEditListener = listener;
    }

    public boolean isEditing() {
        return mEditing;
    }

    public ViewGroup enterEditMode() {
        ViewGroup editBar = getEditBar();
        if (mEditing) {
            return editBar;
        }
        mEditing = true;
        if (mEditListener != null) {
            mEditListener.onEditStart();
        }

        mRoot.installOverlay(editBar, mEditBarLp);

        editBar.setAlpha(0.3f);
        editBar.setTranslationY(mEditBarHeight);
        editBar.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(LauncherAnimUtils.getShortAnimDuration())
                .setInterpolator(LauncherAnimUtils.OVERSHOOT)
                .setListener(null)
                .start();
        return editBar;
    }

    public void reloadLocalWallpaper() {
        if (mAdapter != null) {
            mAdapter.reloadLocalWallpaper();
        }
    }

    public void exitEditMode(boolean affirmative) {
        if (!mEditing) {
            return;
        }

        mEditing = false;
        if (mEditListener != null) {
            mEditListener.onEditEnd(affirmative);
        }

        final View editBar = getEditBar();
        editBar.animate()
                .alpha(0f)
                .setDuration(LauncherAnimUtils.getShortAnimDuration())
                .setInterpolator(LauncherAnimUtils.ACCELERATE_QUAD)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setEditCount(0);
                        mRoot.uninstallOverlay(getEditBar());
                    }
                })
                .start();
    }

    public void setEditCount(int editCount) {
        if (editCount < 0) {
            HSLog.w(TAG, "Negative editCount: " + editCount);
            return;
        }
        String text;
        TextView okButton = mEditOkButton;
        if (editCount == 0) {
            text = getResources().getString(R.string.customize_my_wallpapers_remove_btn);
            okButton.setTextColor(ContextCompat.getColor(getContext(), R.color.material_text_black_hint));
            okButton.setOnClickListener(null);
        } else {
            text = getResources().getString(R.string.customize_my_wallpapers_remove_btn) + " (" + editCount + ")";
            okButton.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
            okButton.setOnClickListener(this);
        }
        okButton.setText(text);
    }

    private ViewGroup getEditBar() {
        if (mEditBar == null) {
            mEditBar = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.wallpaper_list_edit_bottom_bar,
                    (ViewGroup) mRoot.getOverlayee(), false);
            TextView okButton = ViewUtils.findViewById(mEditBar, R.id.wallpaper_list_edit_bottom_bar_ok_btn);
            View cancelButton = ViewUtils.findViewById(mEditBar, R.id.wallpaper_list_edit_bottom_bar_cancel_btn);
            cancelButton.setOnClickListener(this);
            mEditOkButton = okButton;
            setEditCount(0);
        }

        mEditBarLp.bottomMargin = 0;
        return mEditBar;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.wallpaper_list_edit_bottom_bar_ok_btn) {
            exitEditMode(true);
        } else if (i == R.id.wallpaper_list_edit_bottom_bar_cancel_btn) {
            exitEditMode(false);
        }
    }

    public interface PageEditListener {
        void onEditStart();

        void onEditEnd(boolean affirmative);
    }

    public class LocalCustomizePagerAdapter extends PagerAdapter {
        private int[] TITLE_IDS = new int[]{
                R.string.customize_wallpapers_title_local,
        };

        private Context mContext;
        private boolean mIsRtl;

        private RecyclerView mWallpaperTabContent;

        LocalCustomizePagerAdapter(Context context) {
            mContext = context;
            mIsRtl = Dimensions.isRtl();
        }

        private void reloadLocalWallpaper() {
            if (mWallpaperTabContent != null && mWallpaperTabContent.getAdapter() != null) {
                List<WallpaperInfo> infos = WallpaperMgr.getInstance().getLocalWallpapers();
                ((LocalWallpaperGalleryAdapter) mWallpaperTabContent.getAdapter()).reload(infos);
            }
        }

        private RecyclerView getWallpaperTabContent() {
            return mWallpaperTabContent;
        }

        private boolean hasCheckIndies() {
            return ((LocalWallpaperGalleryAdapter) mWallpaperTabContent.getAdapter()).hasCheckedIndices();
        }

        @Override
        public int getCount() {
            return TITLE_IDS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getString(TITLE_IDS[mIsRtl ? getCount() - 1 - position : position]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int positionAbsolute) {
            int position = Utils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            switch (position) {
                case TAB_INDEX_WALLPAPER:
                    FrameLayout myWallpaperContainer = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.local_wallpaper_view, container, false);

                    RecyclerView wallpaperTabContent = ViewUtils.findViewById(myWallpaperContainer, R.id.local_wallpaper_recycler_view);
                    wallpaperTabContent.setHasFixedSize(true);
                    LocalWallpaperGalleryAdapter adapter = new LocalWallpaperGalleryAdapter(mContext, LocalCustomizePage.this);
                    final int spanCount = 2;
                    GridLayoutManager layoutManager = new SafeGridLayoutManager(mContext, spanCount);
                    wallpaperTabContent.setLayoutManager(layoutManager);
                    wallpaperTabContent.addItemDecoration(new GridItemDecoration(Dimensions.pxFromDp(2)));
                    wallpaperTabContent.setAdapter(adapter);

                    RecyclerViewAnimator recyclerViewAnimator = new RecyclerViewAnimator();
                    recyclerViewAnimator.setAddDuration(200);
                    wallpaperTabContent.setItemAnimator(recyclerViewAnimator);
                    wallpaperTabContent.getRecycledViewPool().setMaxRecycledViews(4, 10);
                    mWallpaperTabContent = wallpaperTabContent;

                    /*FloatingActionButton fab = ViewUtils.findViewById(myWallpaperContainer, R.id.fab_upload);
                    fab.setOnClickListener((v) -> {
                        if (Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getBoolean(PREFS_KEY_CC0_USER_AGREED, false)) {
                            getContext().startActivity(new Intent(getContext(), UploadWallpaperActivity.class));
                        } else {
                            getContext().startActivity(new Intent(getContext(), Cc0ProtocolActivity.class));

                        }

                        LauncherAnalytics.logEvent("Wallpaper_Mine_Userupload_Icon_Clicked");
                    });*/
                    container.addView(myWallpaperContainer, 0);
                    return myWallpaperContainer;
            }
            throw new IllegalArgumentException("Invalid position: " + positionAbsolute);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
