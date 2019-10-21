package com.honeycomb.colorphone.wallpaper;

/**
 * Created by sundxing on 2018/6/30.
 */

public class GLParams {
    public static float eyeZ = 2f;
    public static float aspect = 1f;
    public static float near = 1f;
    public static float far = 50f;
    public static float fovy = 45f; // angle in degree

    public static float offsetRatio = 0.6f;
    public static float[] gravityConFactors = new float[4];
    public static float rotateFactor = 0.35f;
    public static float rotateMaxDegree = 10;

    static {
        gravityConFactors[0] = -1;
        gravityConFactors[1] = -0.5f;
        gravityConFactors[2] = -0.1f;
    }
}
