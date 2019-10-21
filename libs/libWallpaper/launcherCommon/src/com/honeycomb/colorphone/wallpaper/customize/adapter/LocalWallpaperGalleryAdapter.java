package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.honeycomb.colorphone.Manager;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.view.ImagePressedTouchListener;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperProvider;
import com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.wallpaper.customize.activity.MyWallpaperActivity;
import com.honeycomb.colorphone.wallpaper.customize.activity.WallpaperEditActivity;
import com.honeycomb.colorphone.wallpaper.customize.activity.WallpaperPreviewActivity;
import com.honeycomb.colorphone.wallpaper.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.wallpaper.customize.view.LocalCustomizePage;
import com.honeycomb.colorphone.wallpaper.desktop.util.SetWallpaperResultLogUtils;
import com.honeycomb.colorphone.wallpaper.livewallpaper.BaseWallpaperService;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.honeycomb.colorphone.wallpaper.view.RatioImageView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data provider for local theme gallery.
 */
public class LocalWallpaperGalleryAdapter extends RecyclerView.Adapter<LocalWallpaperGalleryAdapter.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, LocalCustomizePage.PageEditListener,
        MyWallpaperActivity.ActivityResultHandler {

    private static final String TAG = LocalWallpaperGalleryAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_ADD_BUTTON = -2;
    private static final int VIEW_TYPE_WALLPAPER = 1;

    private static final int TAG_ADD_BUTTON = -1;

    private static final int DISABLED_TINT = Color.argb(90, 0, 0, 0);
    private final List<Integer> mCheckedIndices = new ArrayList<>();
    // Wallpaper file path -> Uri.toString() cache
    private final Map<String, String> mPathUriMap = new HashMap<>();
    private List<WallpaperInfo> mWallpapers;
    private Activity mContext;
    private LayoutInflater mInflater;
    private LocalCustomizePage mPage;
    private static WallpaperInfo sApplyingWallpaper;

    public LocalWallpaperGalleryAdapter(Context context, LocalCustomizePage page) {
        mContext = (Activity) context;
        mInflater = LayoutInflater.from(mContext);
        mPage = page;

        ((MyWallpaperActivity) mContext).addActivityResultHandler(this);
        page.setPageEditListener(this);

        mWallpapers = WallpaperMgr.getInstance().getLocalWallpapers();

        // Delete useless image files
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                File dir = CommonUtils.getDirectory(LiveWallpaperConsts.Files.LOCAL_DIRECTORY);
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            boolean isNeed = false;
                            for (WallpaperInfo wallpaperInfo : mWallpapers) {
                                if (wallpaperInfo.getType() == WallpaperInfo.WALLPAPER_TYPE_GALLERY) {
                                    if (file.getAbsolutePath().equals(wallpaperInfo.getPath()) ||
                                            file.getAbsolutePath().equals(wallpaperInfo.getPath()
                                                    + LiveWallpaperConsts.Files.LOCAL_WALLPAPER_THUMB_SUFFIX)) {
                                        isNeed = true;
                                        break;
                                    }
                                }
                            }

                            if (!isNeed) {
                                boolean deleted = file.delete();
                                HSLog.d(TAG, "Delete file: " + file + ", success: " + deleted);
                            }
                        }
                    }
                }
            }
        });
    }


    public void reload(List<WallpaperInfo> wallpaperInfos) {
        mWallpapers = wallpaperInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_ADD_BUTTON;
        } else {
            WallpaperInfo wallpaper = mWallpapers.get(position - 1);
            return wallpaper.getType();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        ViewHolder holder;
        if (viewType == VIEW_TYPE_ADD_BUTTON) {
            itemView = mInflater.inflate(R.layout.local_wallpaper_gallery_add_btn, parent, false);
            holder = new AddButtonViewHolder(itemView);
        } else {
            itemView = mInflater.inflate(R.layout.local_wallpaper_gallery_item, parent, false);
            holder = new WallpaperViewHolder(itemView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        boolean isEditing = mPage.isEditing();

        if (getItemViewType(position) == VIEW_TYPE_ADD_BUTTON) {
            refreshAddButton((AddButtonViewHolder) holder, isEditing);
            return;
        }

        // Terminology:
        // - position: absolute index in the list, including the add button (eg. the first wallpaper has position 1)
        // - wallpaperIndex: exclude the add button (eg. the first wallpaper has index 0)
        final int wallpaperIndex = position - 1;
        WallpaperInfo wallpaper = mWallpapers.get(wallpaperIndex);

        int type = wallpaper.getType();
        final WallpaperViewHolder wallpaperHolder = (WallpaperViewHolder) holder;
        switch (type) {
            case WallpaperInfo.WALLPAPER_TYPE_BUILT_IN:
                String uri = "drawable://" + wallpaper.getBuiltInDrawableId();
                int thumbnail = WallpaperMgr.BUILT_IN_WALLPAPER_LOCAL_THUMBNAIL_IDS[0];
                GlideApp.with(mContext)
                        .asBitmap()
                        .load(Uri.parse(uri))
                        .placeholder(thumbnail)
                        .into(wallpaperHolder.thumbnail);
                break;
            case WallpaperInfo.WALLPAPER_TYPE_ONLINE:
            case WallpaperInfo.WALLPAPER_TYPE_3D:
            case WallpaperInfo.WALLPAPER_TYPE_LIVE:
                loadRequestIntoImageView(GlideApp.with(mContext).asBitmap().load(wallpaper.getThumbnailUrl()), wallpaperHolder.thumbnail);
                break;
            case WallpaperInfo.WALLPAPER_TYPE_GALLERY:
                File file = new File(wallpaper.getPath());
                if (file.exists()) {
                    loadRequestIntoImageView(GlideApp.with(mContext).asBitmap().load(file), wallpaperHolder.thumbnail);
                }
                break;
        }

        refreshItem(wallpaperHolder, position);
    }

    private void loadRequestIntoImageView(GlideRequest request, ImageView target) {
        request.centerCrop()
                .placeholder(R.drawable.wallpaper_loading)
                .error(R.drawable.wallpaper_load_failed)
                .transition(BitmapTransitionOptions.withCrossFade(350))
                .into(target);
    }

    private Uri getUri(String path) {
        String uri = mPathUriMap.get(path);
        if (uri == null) {
            File file = new File(path);
            uri = Uri.fromFile(file).toString();
            mPathUriMap.put(path, uri);
        }
        return Uri.parse(uri);
    }

    /**
     * Refresh check mark visibility, user input handler, tags.
     */
    private void refreshItem(WallpaperViewHolder holder, int position) {
        final int wallpaperIndex = position - 1;
        WallpaperInfo wallpaper = mWallpapers.get(wallpaperIndex);

        boolean isEditing = mPage.isEditing();
        boolean isBuiltIn = (wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN);
        boolean is3D = (wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_3D);
        boolean isLive = (wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_LIVE);
        boolean isDisabled = (isEditing && (isBuiltIn));

        holder.thumbnail.setTag(R.id.wallpaper_position, wallpaperIndex);
        holder.thumbnail.setOnClickListener(isDisabled ? null : this);
        holder.thumbnail.setOnLongClickListener(isEditing ? null : this);
        holder.thumbnail.setOnTouchListener(isDisabled ? null : new ImagePressedTouchListener(holder.thumbnail));
        holder.thumbnail.setColorFilter(isDisabled ? DISABLED_TINT : Color.TRANSPARENT);

        if (isEditing && !isDisabled) {
            holder.checkMark.setVisibility(View.VISIBLE);
            if (mCheckedIndices.contains(wallpaperIndex)) {
                holder.checkMark.setImageResource(R.drawable.local_wallpaper_checked);
            } else {
                holder.checkMark.setImageResource(R.drawable.local_wallpaper_unchecked);
            }
        } else {
            holder.checkMark.setVisibility(View.INVISIBLE);
        }
        holder.mark3D.setVisibility(is3D ? View.VISIBLE : View.INVISIBLE);
        holder.markLive.setVisibility(isLive ? View.VISIBLE : View.INVISIBLE);
    }

    private void refreshAddButton(AddButtonViewHolder holder, boolean isEditing) {
        holder.button.setTag(R.id.wallpaper_position, TAG_ADD_BUTTON);
        holder.button.setOnClickListener(isEditing ? null : this);
        holder.button.setOnLongClickListener(isEditing ? null : this);
        holder.button.setOnTouchListener(isEditing ? null :
                new ImagePressedTouchListener(holder.overMask, ImagePressedTouchListener.MODE_REPLACE_DRAWABLE));
        holder.overMask.setImageDrawable(new ColorDrawable(isEditing ? DISABLED_TINT : Color.TRANSPARENT));
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag(R.id.wallpaper_position);
        if (tag != null) {
            int index = (int) tag;
            if (mPage.isEditing()) {
                ImageView view = (ImageView) ((View) v.getParent()).findViewById(R.id.local_wallpaper_check_mark);
                onEditClick(index, view);
            } else {
                onNormalClick(index);
            }
        }
    }

    private void onNormalClick(int wallpaperIndex) {
        if (wallpaperIndex == TAG_ADD_BUTTON) {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
            Navigations.startActivityForResultSafely(mContext, chooserIntent, CustomizeActivity.REQUEST_CODE_PICK_WALLPAPER);
            LauncherAnalytics.logEvent("Wallpaper_Mine_MyWallpaper_Add_Btn_Clicked");
            Manager.getInstance().getDelegate().logEvent("Settings_MyWallpaper_AddWallpaper_Clicked");
        } else {
            WallpaperInfo wallpaper = mWallpapers.get(wallpaperIndex);
            if (wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_3D) {
                sApplyingWallpaper = wallpaper;
                CustomizeUtils.preview3DWallpaper(mContext, wallpaper);
            } else if (wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_LIVE) {
                sApplyingWallpaper = wallpaper;
                CustomizeUtils.previewLiveWallpaper(mContext, wallpaper);
            } else {
                Intent intent = WallpaperPreviewActivity.getLaunchIntent(mContext, WallpaperMgr.Scenario.LOCAL,
                        null, wallpaperIndex, null);
                mContext.startActivity(intent);
            }
        }
    }

    @Override
    public void handleActivityResult(Activity activity, int requestCode, int resultCode, final Intent data) {
        if (activity == mContext
                && requestCode == CustomizeActivity.REQUEST_CODE_PICK_WALLPAPER
                && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toasts.showToast(R.string.local_wallpaper_pick_error);
                return;
            }

            Intent intent = WallpaperEditActivity.getLaunchIntent(mContext, data);
            mContext.startActivity(intent);
        } else if (requestCode == CustomizeActivity.REQUEST_CODE_APPLY_3D_WALLPAPER) {
            Preferences prefs = Preferences.getDefault();
            if (CustomizeUtils.isApplySuccessful(activity, resultCode)) {
                if (sApplyingWallpaper != null) {
                    WallpaperInfo wallpaper = sApplyingWallpaper;
                    sApplyingWallpaper = null;
                    String newWallpaperName = wallpaper.getSource();

                    SetWallpaperResultLogUtils.logLiveWallpaperUseEvents(newWallpaperName,
                            wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_LIVE);
                    HSLog.d("SetLiveWallpaper", "LWGA, set " + newWallpaperName);
                    prefs.putString(LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, newWallpaperName);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(WallpaperProvider.BUNDLE_KEY_WALLPAPER, wallpaper);
                    ContentResolver contentResolver = HSApplication.getContext().getContentResolver();
                    contentResolver.call(WallpaperProvider.CONTENT_URI, WallpaperProvider.METHOD_APPLY_WALLPAPER, "", bundle);

                }
                Toasts.showToast("设置成功");
            }
            prefs.putBoolean(LiveWallpaperConsts.PREF_KEY_IS_PREVIEW_MODE, false);

            new Handler().postDelayed(() -> {
                Intent liveWallpaperApplied = new Intent();
                liveWallpaperApplied.setAction(BaseWallpaperService.LIVE_WALLPAPER_APPLIED);
                mContext.sendBroadcast(liveWallpaperApplied);
            }, 200);
        }
    }

    private void onEditClick(int wallpaperIndex, ImageView view) {
        //noinspection StatementWithEmptyBody
        if (wallpaperIndex == TAG_ADD_BUTTON) {
            // No operation
        } else {
            view.setVisibility(View.VISIBLE);
            if (mCheckedIndices.contains(wallpaperIndex)) {
                mCheckedIndices.remove(Integer.valueOf(wallpaperIndex));
                view.setImageResource(R.drawable.local_wallpaper_unchecked);
            } else {
                mCheckedIndices.add(wallpaperIndex);
                view.setImageResource(R.drawable.local_wallpaper_checked);
            }
            mPage.setEditCount(mCheckedIndices.size());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Object tag = v.getTag();
        if (tag != null) {
            int index = (int) tag;
            if (index == TAG_ADD_BUTTON) {
                onClick(v);
            } else {
                mPage.enterEditMode();
                WallpaperInfo wallpaper = mWallpapers.get(index);
                if (wallpaper.getType() != WallpaperInfo.WALLPAPER_TYPE_BUILT_IN) {
                    ImageView view = (ImageView) ((View) v.getParent())
                            .findViewById(R.id.local_wallpaper_check_mark);
                    onEditClick(index, view);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onEditStart() {
        notifyDataSetChanged();
    }

    public void onEditEnd(boolean affirmative) {
        if (affirmative) {
            List<WallpaperInfo> removed = new ArrayList<>(mCheckedIndices.size());

            for (int index : mCheckedIndices) {
                removed.add(mWallpapers.get(index));
            }
            remove(mCheckedIndices);
            WallpaperMgr.getInstance().removeLocalWallpapers(removed);
            mCheckedIndices.clear();
        } else {
            mCheckedIndices.clear();
            notifyDataSetChanged();
        }
    }

    private void remove(List<Integer> positions, int firstPosition) {
        Collections.sort(positions);
        List<Integer> trueRemoved = new ArrayList<>();
        int detla = 0;
        for (Integer position : positions) {
            trueRemoved.add(position - detla);
            detla++;
        }
        for (Integer integer : trueRemoved) {
            mWallpapers.remove((int) integer);
            notifyItemRemoved(integer + firstPosition);
        }
        notifyItemRangeChanged(0, getItemCount());
    }

    public void remove(List<Integer> positions) {
        remove(positions, 1);
    }

    public boolean hasCheckedIndices() {
        return !mCheckedIndices.isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class WallpaperViewHolder extends ViewHolder {
        RatioImageView thumbnail;
        ImageView checkMark;
        ImageView mark3D;
        ImageView markLive;

        WallpaperViewHolder(View itemView) {
            super(itemView);
            thumbnail = ViewUtils.findViewById(itemView, R.id.wallpaper_thumbnail);
            checkMark = ViewUtils.findViewById(itemView, R.id.local_wallpaper_check_mark);
            mark3D = ViewUtils.findViewById(itemView, R.id.local_wallpaper_3d_mark);
            markLive = ViewUtils.findViewById(itemView, R.id.local_wallpaper_live_mark);
        }
    }

    private static class AddButtonViewHolder extends ViewHolder {
        ImageView button;

        /**
         * For showing pressed or disabled states
         */
        ImageView overMask;

        AddButtonViewHolder(View itemView) {
            super(itemView);
            button = ViewUtils.findViewById(itemView, R.id.local_wallpaper_add_btn);
            overMask = ViewUtils.findViewById(itemView, R.id.local_wallpaper_add_btn_over_mask);
            button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }
}
