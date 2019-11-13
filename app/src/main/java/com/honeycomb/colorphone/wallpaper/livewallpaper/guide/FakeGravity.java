package com.honeycomb.colorphone.wallpaper.livewallpaper.guide;


/**
 * Created by sundxing on 2018/5/26.
 */

public class FakeGravity implements RotationMaker.Callback {
    private static final float G = 9.8f;
    private final float gravityOffset[] = new float[]{0, 0, 0};
    public volatile boolean enable = false;

    @Override
    public void onRotateX(float angle) {
        enable = true;
        gravityOffset[1] = getGravity(angle);
    }

    @Override
    public void onRotateY(float angle) {
        enable = true;
        gravityOffset[0] = getGravity(angle);
    }

    @Override
    public void onRotateEnd() {
        gravityOffset[0] = 0f;
        gravityOffset[1] = 0f;
        enable = false;
    }

    private float getGravity(float angle) {
        return (float) (G * Math.sin(angle * Math.PI / 180f));
    }

    public void adjust(float[] gSenor) {
        if (enable) {
            gSenor[0] = 0.9f * gSenor[0] + 0.1f * (gSenor[0] + gravityOffset[0]);
            gSenor[1] = 0.9f * gSenor[1] + 0.1f * (gSenor[1] + gravityOffset[1]);
        }
    }
}
