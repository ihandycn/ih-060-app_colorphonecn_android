package com.acb.libwallpaper.live.livewallpaper.confetti.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.annimon.stream.Stream;
import com.acb.libwallpaper.live.livewallpaper.BaseWallpaperManager;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.livewallpaper.Program;
import com.acb.libwallpaper.live.livewallpaper.ShaderRenderer;
import com.acb.libwallpaper.live.livewallpaper.confetti.CommonConfetti;
import com.acb.libwallpaper.live.livewallpaper.confetti.ConfettiFilter;
import com.acb.libwallpaper.live.livewallpaper.confetti.ConfettiManager;
import com.acb.libwallpaper.live.livewallpaper.confetti.ConfettiSource;
import com.acb.libwallpaper.live.livewallpaper.confetti.confetto.Confetto;
import com.acb.libwallpaper.live.livewallpaper.confetti.fade.Trivial;
import com.ihs.commons.utils.HSLog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Handles OpenGL operations to render a confetti.
 */
public abstract class ConfettiRenderer {

    private static final String TAG = ConfettiRenderer.class.getSimpleName();

    private static final String ALPHA_CLASS_PATH = "com.huandong.wallpaper.live.livewallpaper.confetti.fade";
    private static final String SCALE_CLASS_PATH = "com.huandong.wallpaper.live.livewallpaper.confetti.scale";

    public abstract static class GLContext {
    }

    public static class TextureRecord {
        public Bitmap bitmap;
        public final float ratio;
        public final int settingId;
        public final long category;
        public final int width, height;
        public int textureId;

        public TextureRecord(Bitmap bitmap, float ratio, int settingId, long category) {
            this.bitmap = bitmap;
            this.ratio = ratio;
            this.settingId = settingId;
            this.category = category;
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
        }

        void releaseBitmap() {
            bitmap = null;
        }
    }

    ByteBuffer vertexBuffer;
    ShaderRenderer.TextureBinder textureBinder;

    final float resolution[] = new float[]{0, 0};

    boolean needFastForward;
    long forwardTime;

    int particleProgram = 0;

    final List<ConfettiRenderer.TextureRecord> textures = new ArrayList<>();

    final List<CommonConfetti> bgConfettis;
    final List<CommonConfetti> touchConfettis;
    final List<CommonConfetti> clickConfettis;

    private BaseWallpaperManager manager;
    private String wallpaperName;
    private List<CommonConfetti> stopEmissionConfettis;

    int srcBlendMode;

    ConfettiRenderer(ByteBuffer vertexBuffer, ShaderRenderer.TextureBinder textureBinder, BaseWallpaperManager manager, String wallpaperName) {
        this.vertexBuffer = vertexBuffer;
        this.textureBinder = textureBinder;
        this.manager = manager;
        this.wallpaperName = wallpaperName;

        bgConfettis = new ArrayList<>();
        touchConfettis = new ArrayList<>();
        clickConfettis = new ArrayList<>();
        stopEmissionConfettis = new ArrayList<>();
    }

    public void onSurfaceCreated() {
        indexLocations();
    }

    public void initWallpaper() {
        touchConfettis.clear();
        clickConfettis.clear();
        createTextures();
        startBackgroundConfetti();
        needFastForward = checkBgFastForward();
    }

    private boolean checkBgFastForward() {
        boolean hasClickRes = !manager.getConfettiAttrs(wallpaperName, LiveWallpaperConsts.CLICK).isEmpty();
        boolean hasTouchRes = !manager.getConfettiAttrs(wallpaperName, LiveWallpaperConsts.TOUCH).isEmpty();
        return !hasTouchRes && hasClickRes;
    }

    public void onSurfaceChanged(float width, float height) {
        if (resolution[0] != width || resolution[1] != height) {
            resolution[0] = width;
            resolution[1] = height;

            setupBuffers();
            startBackgroundConfetti();
        }
    }

    public void adjustStartTimeAfterPause(long pauseDuration) {
        synchronized (bgConfettis) {
            for (int i = 0; i < bgConfettis.size(); i++) {
                CommonConfetti bgConfetti = bgConfettis.get(i);
                bgConfetti.getConfettiManager().adjustStartTimeAfterPause(pauseDuration);
            }
        }

        synchronized (touchConfettis) {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<CommonConfetti> iterator = touchConfettis.iterator(); iterator.hasNext(); ) {
                CommonConfetti confetti = iterator.next();
                ConfettiManager confettiManager = confetti.getConfettiManager();
                confettiManager.adjustStartTimeAfterPause(pauseDuration);
            }

            for (Iterator<CommonConfetti> iterator = clickConfettis.iterator(); iterator.hasNext(); ) {
                CommonConfetti confetti = iterator.next();
                ConfettiManager confettiManager = confetti.getConfettiManager();
                confettiManager.adjustStartTimeAfterPause(pauseDuration);
            }
        }
    }

    public void updateWallpaper(String name) {
        wallpaperName = name;
    }

    public abstract void createTextures();

    public abstract void indexLocations();

    public abstract void setupBuffers();

    abstract void deleteTextures();

    @MainThread
    public void touchAt(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        synchronized (touchConfettis) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopEmission();
                    stopEmissionConfettis.clear();
                    applyTouch(LiveWallpaperConsts.TOUCH, x, y, touchConfettis);
                    bombClickableConfetti(e);
                    break;
                case MotionEvent.ACTION_MOVE:
                    bombClickableConfetti(e);
                    if (!touchConfettis.isEmpty()) {
                        CommonConfetti movingConfetti = touchConfettis.get(touchConfettis.size() - 1);
                        if(movingConfetti.isFromTouch()) {
                            movingConfetti.getConfettiManager()
                                    .setConfettiSource(new ConfettiSource((int) x, (int) y));
                        }

                        stopEmissionConfettis.add(movingConfetti);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    stopEmission();
                    break;
            }
        }
    }

    private void bombClickableConfetti(MotionEvent e) {
        synchronized (bgConfettis) {
            for (CommonConfetti commonConfetti : bgConfettis) {
                if (commonConfetti.isClickable()) {
                    Confetto confetto = commonConfetti.getConfettiManager().touchAt(e);
                    if (confetto != null) {
                        applyTouch(LiveWallpaperConsts.CLICK, confetto.getCurrentX(),
                                confetto.getCurrentY(), clickConfettis);
                    }
                }
            }
        }
    }

    private void stopEmission() {
        if (!stopEmissionConfettis.isEmpty()) {
            // Stop emission
            final Iterator<CommonConfetti> iterator = stopEmissionConfettis.iterator();
            while (iterator.hasNext()) {
                iterator.next().getConfettiManager().setEmissionDuration(0);
                iterator.remove();
            }
        }
    }

    private void applyTouch(long category, float centerX,
                            float centerY, List<CommonConfetti> out) {
        HSLog.d(TAG, "apply " + category + " confetti configured in wallpaper XML");
        ArrayList<HashMap<String, Object>> config = manager.getConfettiAttrs(wallpaperName, category);
        if (!config.isEmpty() && !config.get(0).isEmpty()) {
            CommonConfetti confetti = createConfetti(config.get(0), centerX, centerY, category, 0);
            out.add(confetti);
            if (LiveWallpaperConsts.CLICK == category) {
                confetti.infiniteSingle();
            } else {
                confetti.infinite(getValue(config.get(0), "emissionRate", 25));
                confetti.setFromTouch(getValue(config.get(0),"touchPoint", true));
            }
            stopEmissionConfettis.add(confetti);
        } else {
            HSLog.d(TAG, "No " + category + " confetti configured in wallpaper XML");
        }
    }

    private void startBackgroundConfetti() {
        synchronized (bgConfettis) {
            for (CommonConfetti bgConfetti : bgConfettis) {
                bgConfetti.getConfettiManager().terminate();
            }
            bgConfettis.clear();
            ArrayList<HashMap<String, Object>> configs = manager.getConfettiAttrs(wallpaperName, LiveWallpaperConsts.BACKGROUND);
            for (int index = 0; index < configs.size(); index++) {
                HashMap<String, Object> config = configs.get(index);
                if (!config.isEmpty()) {
                    CommonConfetti confetti = createConfetti(config, 0f, 0f,
                            LiveWallpaperConsts.BACKGROUND, index);
                    bgConfettis.add(confetti);

                    // Start confetti generation
                    confetti.infinite(getValue(config, "emissionRate", 25));
                    confetti.setClickable(getValue(config, "clickable", false));
                } else {
                    HSLog.d(TAG, "No background confetti configured in wallpaper XML");
                }
            }
        }
    }

    private boolean configSourceFromConfig(long category, Map<String, Object> config) {
        if (LiveWallpaperConsts.BACKGROUND == category) {
            return true;
        }

        boolean isFromTouch = getValue(config, "touchPoint", true);
        return !isFromTouch;
    }

    private CommonConfetti createConfetti(Map<String, Object> config,
                                          float srcX, float srcY, long category, int settingId) {
        ConfettiSource confettiSource;
        if (configSourceFromConfig(category, config)) {
            float sourceFromXConfig = getValue(config, "sourceFromX", 0);
            int sourceFromX = (int) (resolution[0] * sourceFromXConfig);
            int sourceFromXDeviation = (int) (resolution[0] * getValue(config, "sourceFromXDeviation", 0));

            float sourceFromYConfig = getValue(config, "sourceFromY", 0);
            int sourceFromY = (int) (resolution[1] * sourceFromYConfig);
            int sourceFromYDeviation = (int) (resolution[1] * getValue(config, "sourceFromYDeviation", 0));

            int sourceToX = (int) (resolution[0] * getValue(config, "sourceToX", sourceFromXConfig));
            int sourceToXDeviation = (int) (resolution[0] * getValue(config, "sourceToXDeviation", 0));

            int sourceToY = (int) (resolution[1] * getValue(config, "sourceToY", sourceFromYConfig));
            int sourceToYDeviation = (int) (resolution[1] * getValue(config, "sourceToYDeviation", 0));

            confettiSource = new ConfettiSource(
                    sourceFromX, sourceFromXDeviation,
                    sourceFromY, sourceFromYDeviation,
                    sourceToX, sourceToXDeviation,
                    sourceToY, sourceToYDeviation);
        } else {
            confettiSource = new ConfettiSource((int) srcX, (int) srcY);
        }

        final CommonConfetti confetti = new CommonConfetti();
        ConfettiFilter confettiFilter = new ConfettiFilter() {
            @Override
            public boolean filter(Confetto confetto) {
                float initialVelocity = confetto.getInitialVelocityScalar();
                return getValueDensityRatioCorrected(config, "minVelocity", Float.MIN_VALUE) <= initialVelocity
                        && initialVelocity <= getValueDensityRatioCorrected(config, "maxVelocity", Float.MAX_VALUE);
            }

            @Override
            public int maxIterationCount() {
                return (int) getValue(config, "maxSelectIterations", 5);
            }
        };
        confetti.configureConfetti(textures, confettiFilter, confettiSource,
                (int) resolution[0], (int) resolution[1], category, settingId);

        confetti.getConfettiManager()
                .setDelay(getValue(config, "delay", 0),
                        getValue(config, "delayDeviation", 0))
                .setTTL((long) getValue(config, "ttl", Long.MAX_VALUE))
                .setNumInitialCount((int) getValue(config, "initNum", 0))
                .setVelocityX(
                        getValueDensityRatioCorrected(config, "velocityX", 0),
                        getValueDensityRatioCorrected(config, "velocityDeviationX", 0))
                .setVelocityY(
                        getValueDensityRatioCorrected(config, "velocityY", 0),
                        getValueDensityRatioCorrected(config, "velocityDeviationY", 0))
                .setAccelerationX(
                        getValueDensityRatioCorrected(config, "accelerationX", 0),
                        getValueDensityRatioCorrected(config, "accelerationDeviationX", 0))
                .setAccelerationY(
                        getValueDensityRatioCorrected(config, "accelerationY", 0),
                        getValueDensityRatioCorrected(config, "accelerationDeviationY", 0))
                .setTargetVelocityX(
                        (Float) getValueDensityRatioCorrected(config, "targetVelocityX", null),
                        getValueDensityRatioCorrected(config, "targetVelocityXDeviation", 0))
                .setTargetVelocityY(
                        (Float) getValueDensityRatioCorrected(config, "targetVelocityY", null),
                        getValueDensityRatioCorrected(config, "targetVelocityYDeviation", 0))
                .setInitialRotation(
                        (int) getValue(config, "initialRotation", 0),
                        (int) getValue(config, "initialRotationDeviation", 0))
                .setRotationalVelocity(
                        getValue(config, "rotationalVelocity", 0),
                        getValue(config, "rotationalVelocityDeviation", 0))
                .setRotationalAcceleration(
                        getValue(config, "rotationalAcceleration", 0),
                        getValue(config, "rotationalAccelerationDeviation", 0))
                .setTargetRotationalVelocity(
                        (Float) getValue(config, "targetRotationalVelocity", null),
                        getValue(config, "targetRotationalVelocityDeviation", 0))
                .setInitialScale(
                        getValue(config, "initialScale", 1f),
                        getValue(config, "initialScaleDeviation", 0))
                .setScaleVelocity(
                        getValue(config, "scaleVelocity", 0),
                        getValue(config, "scaleVelocityDeviation", 0))
                .setScaleAcceleration(
                        getValue(config, "scaleAcceleration", 0),
                        getValue(config, "scaleAccelerationDeviation", 0))
                .setTargetScaleVelocity(
                        (Float) getValue(config, "targetScaleVelocity", null),
                        getValue(config, "targetScaleVelocityDeviation", 0))
                .setAlpha(getValue(config, "alpha", 1f),
                        getValue(config, "alphaDeviation", 0f))
                .setDestinationY(getValue(config, "destinationToY", 0f) * resolution[1],
                        getValue(config, "destinationToYDeviation", 0f) * resolution[1])
                .setDestinationX(getValue(config, "destinationToX", 0f) * resolution[0],
                        getValue(config, "destinationToYDeviation", 0f) * resolution[0])
                .setClickable(getValue(config, "clickable", false))
                .setFromTouchPoint(getValue(config,"touchPoint", true))
                .enableFadeOut(createInterpolator((String) config.get("alphaInterpolator"), true))
                .enableScale(createInterpolator((String) config.get("scaleInterpolator"), false));

        srcBlendMode = (int) getValue(config, "blendMode", GLES20.GL_ONE);

        return confetti;
    }

    private Interpolator createInterpolator(String className, boolean isAlpha) {
        if (className == null || className.isEmpty()) {
            return new Trivial();
        }
        Class<?> klass;
        try {
            klass = Class.forName((isAlpha ? ALPHA_CLASS_PATH : SCALE_CLASS_PATH) + "." + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new Trivial();
        }
        try {
            return (Interpolator) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new Trivial();
        }
    }

    private float getValueDensityRatioCorrected(Map<String, Object> config, String key, float defaultVal) {
        return getValue(config, key, defaultVal) * ConfettiManager.DEVICE_INDEPENDENT_CORRECTION;
    }

    private float getValue(Map<String, Object> config, String key, float defaultVal) {
        if (config == null || TextUtils.isEmpty(key)) {
            return defaultVal;
        }

        if (config.containsKey(key)) {
            try {
                return Float.valueOf((String) config.get(key));
            } catch (Exception e) {
                return defaultVal;
            }
        }

        return defaultVal;
    }

    private boolean getValue(Map<String, Object> config, String key, boolean defaultVal) {
        if (config == null || TextUtils.isEmpty(key)) {
            return defaultVal;
        }

        if (config.containsKey(key)) {
            try {
                return Boolean.valueOf((String) config.get(key));
            } catch (Exception e) {
                return defaultVal;
            }
        }

        return defaultVal;
    }

    private Object getValueDensityRatioCorrected(Map<String, Object> config, String key, Object defaultVal) {
        Object obj = getValue(config, key, defaultVal);
        if (obj instanceof Float) {
            obj = ((float) obj) * ConfettiManager.DEVICE_INDEPENDENT_CORRECTION;
        }
        return obj;
    }

    private Object getValue(Map<String, Object> config, String key, Object defaultVal) {
        if (config == null || TextUtils.isEmpty(key)) {
            return defaultVal;
        }

        if (config.containsKey(key)) {
            try {
                return Float.valueOf((String) config.get(key));
            } catch (Exception e) {
                return defaultVal;
            }
        }

        return defaultVal;
    }

    public boolean loadProgram() {
        String[] shaders = new String[2]; // [0] = vertex shader, [1] = fragment shader
        getShaders(shaders);
        return (particleProgram = Program.loadProgram(shaders[0], shaders[1])) != 0;
    }

    abstract void getShaders(String[] shaders);

    public abstract void drawFrame(long now, int fps);

    void generateConfettiBitmaps(List<TextureRecord> outTextures) {
        outTextures.clear();
        ArrayList<TextureRecord> textureRecords = manager.getConfettiTextures(wallpaperName);
        if (textureRecords != null) {
            Stream.of(textureRecords)
                    .withoutNulls()
                    .filter(texture -> texture.bitmap != null
                            && texture.bitmap.getWidth() > 0
                            && texture.bitmap.getHeight() > 0)
                    .forEach(outTextures::add);
        }
    }
}
