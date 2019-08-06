package com.honeycomb.colorphone.customize.livewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.ConfettiRenderer;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.RenderThread;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses live wallpaper XMLs and provides live wallpaper resources.
 */
@SuppressWarnings("SimplifiableIfStatement")
public class LiveWallpaperManager extends BaseWallpaperManager {

    private static final String TAG = LiveWallpaperManager.class.getSimpleName();

    private volatile static LiveWallpaperManager sInstance;

    private final Map<String, LiveWallpaper> mWallpapers = new HashMap<>();
    private BitmapDecoder mBitmapDecoder;
    final String mBaseUrl = CustomizeConfig.getString("", "Application", "Wallpaper", "LiveWallpapers", "BaseUrl");
    private HSPreferenceHelper prefs = HSPreferenceHelper.getDefault();

    public static LiveWallpaperManager getInstance() {
        if (sInstance == null) {
            synchronized (LiveWallpaperManager.class) {
                if (sInstance == null) {
                    sInstance = new LiveWallpaperManager();
                }
            }
        }
        return sInstance;
    }

    private LiveWallpaperManager() {
    }

    public String getWallpaperName() {
        boolean isPreviewMode = prefs.getBoolean(LiveWallpaperConsts.PREF_KEY_IS_PREVIEW_MODE, false);
        String name = prefs.getString(isPreviewMode ? LiveWallpaperConsts.PREF_KEY_PREVIEW_WALLPAPER_NAME : LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, "");
        HSLog.d("SetLiveWallpaper", "getWallpaperName: isPreview=" + isPreviewMode + ", name=" + name);
        return name;
    }

    public void prepareWallpaper() {
        String wallpaperName = getWallpaperName();
        HSLog.d(TAG, "prepareWallpaper" + getWallpaperName());
        if (!isDecoderReady(wallpaperName)) {
            mBitmapDecoder = new BitmapDecoder(wallpaperName);
        }
        mBitmapDecoder.startDecode();
    }

    /**
     * @return Whether meta-data of given wallpaper is parsed from assets or internal storage.
     * Returns {@code false} when meta-data XML is placed remotely and have not yet been downloaded.
     */
    private synchronized boolean initWallpaperIfNeeded(String wallpaperName) {
        if (!mWallpapers.containsKey(wallpaperName)) {
            return init(wallpaperName);
        }
        return true;
    }

    @Override
    public boolean touchable(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            LiveWallpaper wallpaper = mWallpapers.get(wallpaperName);
            return wallpaper.touchable() || wallpaper.isRipple();
        }
        return false;
    }

    @Override
    public boolean is3D(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            LiveWallpaper wallpaper = mWallpapers.get(wallpaperName);
            return wallpaper.is3D() && !wallpaper.touchable();
        }
        return false;
    }

    @Override
    protected int getType(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            return mWallpapers.get(wallpaperName).type;
        }else if (wallpaperName.equals("particleflow")){
            return LiveWallpaperConsts.TYPE_PARTICLE_FLOW;
        }
        return getTypeFromConfig(wallpaperName);
    }

    @SuppressWarnings("unchecked")
    private int getTypeFromConfig(String wallpaperName) {
        List<?> wallpapers3d = CustomizeConfig.getList("3DWallpapers", "Items");
        List<?> wallpapersLive = CustomizeConfig.getList("Application", "Wallpaper", "LiveWallpapers", "Items");

        List wallpapers = new ArrayList<>(wallpapers3d.size() + wallpapersLive.size());
        wallpapers.addAll(wallpapers3d);
        wallpapers.addAll(wallpapersLive);

        for (Object wallpaper : wallpapers) {
            if (!(wallpaper instanceof Map)) {
                continue;
            }
            String name = (String) ((Map) wallpaper).get("Name");
            if (!TextUtils.equals(name, wallpaperName)) {
                continue;
            }
            if (HSMapUtils.optBoolean((Map<String, ?>) wallpaper, false, "IsVideo")) {
                return LiveWallpaperConsts.TYPE_VIDEO;
            } else {
                return LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI;
            }
        }

        return LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI;
    }

    protected boolean isLocal(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            return mWallpapers.get(wallpaperName).isLocal;
        }
        return false;
    }

    @Override
    protected String getShader(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            LiveWallpaper liveWallpaper = mWallpapers.get(wallpaperName);
            if (!TextUtils.isEmpty(liveWallpaper.shader)) {
                return liveWallpaper.shader;
            }
            return liveWallpaper.loadShader(liveWallpaper.shaderName);
        }
        return "";
    }

    @Override
    @RenderThread
    protected Bitmap getShaderTexture(String wallpaperName, String textureName) {
        if (!isDecoderReady(wallpaperName)) {
            if (BuildConfig.DEBUG) {
                HSLog.e(TAG, "mBitmapDecoder not prepare"
                        + " :" + wallpaperName);
            }
            mBitmapDecoder = new BitmapDecoder(wallpaperName);
            mBitmapDecoder.startDecode();
        }

        return mBitmapDecoder.getBitmap(textureName);
    }

    private boolean isDecoderReady(String wallpaperName) {
       return mBitmapDecoder != null
                && !mBitmapDecoder.isOver()
                && TextUtils.equals(mBitmapDecoder.getWallpaperName(), wallpaperName);
    }

    @Override
    public ArrayList<HashMap<String, Object>> getConfettiAttrs(String wallpaperName, long category) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            if (LiveWallpaperConsts.BACKGROUND == category) {
                return mWallpapers.get(wallpaperName).confettiBgSettings;
            } else if (LiveWallpaperConsts.CLICK == category) {
                return mWallpapers.get(wallpaperName).confettiClickSettings;
            } else if (LiveWallpaperConsts.TOUCH == category) {
                return mWallpapers.get(wallpaperName).confettiTouchSettings;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public @NonNull ArrayList<ConfettiRenderer.TextureRecord> getConfettiTextures(String wallpaperName) {
        ArrayList<ConfettiRenderer.TextureRecord> confettiTextures = new ArrayList<>();
        File baseDirectory = Utils.getDirectory(LiveWallpaperConsts.Files.LIVE_DIRECTORY);
        File storedDirectory = new File(baseDirectory, wallpaperName);
        if (storedDirectory.exists() && storedDirectory.isDirectory()) {
            if (storedDirectory.listFiles() == null) {
                return confettiTextures;
            }
            for (File file : storedDirectory.listFiles()) {
                if (file.getName().equals(LiveWallpaperConsts.LIVE_WALLPAPER_CONFETTI_DIRECTORY) && file.isDirectory()) {
                    for (File confetti : file.listFiles()) {
                        Stream.of(mWallpapers.get(wallpaperName).confettiSources)
                                .withoutNulls()
                                .filter(source -> !TextUtils.isEmpty(source.getUrl())
                                        && source.getUrl().contains(confetti.getName()))
                                .map(source -> {
                                    Bitmap bitmap = decodeBitmap(confetti);
                                    if (bitmap == null) {
                                        return null;
                                    }
                                    return new ConfettiRenderer.TextureRecord(bitmap,
                                            source.getRatio(), source.getId(), source.getCategory());
                                })
                                .withoutNulls()
                                .forEach(confettiTextures::add);
                    }
                }
            }
        }

        return confettiTextures;
    }

    @Override
    protected Uri getVideo(String wallpaperName) {
        File baseDirectory = Utils.getDirectory(
                LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
        File videoFile = new File(baseDirectory, "video.mp4");
        return Uri.fromFile(videoFile);
    }

    String getBaseUrl(String wallpaperName) {
        if (initWallpaperIfNeeded(wallpaperName)) {
            return mWallpapers.get(wallpaperName).baseUrl;
        }
        return CustomizeConfig.getString("", "Application", "Wallpaper", "LiveWallpapers", "BaseUrl");
    }

    /**
     * Historically, this returns list of independent image URLs on S3 storage.
     * Since we pack all resources for a wallpaper in a ZIP file, URLs returned by this method
     * are merely resource descriptors used in the naming of local files when they are released
     * from ZIP pack.
     */
    ArrayList<String> getShaderTextureUrls(String wallpaperName) {
        return getTextureUrls(wallpaperName, true);
    }

    /**
     * Historically, this returns list of independent image URLs on S3 storage.
     * Since we pack all resources for a wallpaper in a ZIP file, URLs returned by this method
     * are merely resource descriptors used in the naming of local files when they are released
     * from ZIP pack.
     */
    ArrayList<String> getConfettiTextureUrls(String wallpaperName) {
        return getTextureUrls(wallpaperName, false);
    }

    private boolean init(String wallpaperName) {
        LiveWallpaper wallpaper = new LiveWallpaper(wallpaperName, mBaseUrl);
        if (wallpaper.successFlag()) {
            mWallpapers.put(wallpaperName, wallpaper);
            return true;
        }
        return false;
    }

    private Bitmap decodeBitmap(File wallpaper) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(wallpaper.getPath());
        } catch (Exception ignored) {
            if (BuildConfig.DEBUG) {
                HSLog.e(TAG, "picture decode failed: file path = " + wallpaper.getPath());
            }
        }
        return bitmap;
    }

    private ArrayList<String> getTextureUrls(String wallpaperName, boolean isShader) {
        ArrayList<String> urls = new ArrayList<>();
        if (initWallpaperIfNeeded(wallpaperName)) {
            if (isShader) {
                urls.addAll(mWallpapers.get(wallpaperName).shaderTexTureUrls);
            } else {
                Stream.of(mWallpapers.get(wallpaperName).confettiSources)
                        .withoutNulls()
                        .filter(source -> !TextUtils.isEmpty(source.getUrl())
                                && !urls.contains(source.getUrl()))
                        .forEach(source -> urls.add(source.getUrl()));
            }
        }
        return urls;
    }
}
