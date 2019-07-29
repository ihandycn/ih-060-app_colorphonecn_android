package com.honeycomb.colorphone.customize.livewallpaper;

/**
 * Constants for uniform variable (input) names used in fragment shaders.
 */
class ShaderConstants {

    /** (float) Elapsed time (in seconds) since start of drawing for current surface. */
    static final String UNIFORM_TIME = "time";

    /** (float) Device density ratio. */
    static final String UNIFORM_DENSITY_RATIO = "densityRatio";

    static final String UNIFORM_SECOND = "second";
    static final String UNIFORM_SUBSECOND = "subsecond";
    static final String UNIFORM_FTIME = "ftime";
    static final String UNIFORM_RESOLUTION = "resolution";
    static final String UNIFORM_TOUCH = "touch";
    static final String UNIFORM_MOUSE = "mouse";
    static final String UNIFORM_POINTER_COUNT = "pointerCount";
    static final String UNIFORM_POINTERS = "pointers";

    /** Touch history. */
    static final String UNIFORM_POINTER_HISTORY_SIZE = "pointerHistorySize";
    static final String UNIFORM_POINTER_HISTORY = "pointerHistory";

    static final String UNIFORM_GRAVITY = "gravity";
    static final String UNIFORM_LINEAR = "linear";
    static final String UNIFORM_ROTATION = "rotation";
    static final String UNIFORM_MAGNETIC = "magnetic";
    static final String UNIFORM_ORIENTATION = "orientation";
    static final String UNIFORM_LIGHT = "light";
    static final String UNIFORM_PRESSURE = "pressure";
    static final String UNIFORM_PROXYMITY = "proximity";
    static final String UNIFORM_OFFSET = "offset";
    static final String UNIFORM_BATTERY = "battery";
    static final String UNIFORM_DATE = "date";
    static final String UNIFORM_START_RANDOM = "startRandom";
    static final String UNIFORM_BACKBUFFER = "backbuffer";
    static final String UNIFORM_CAMERA_ORIENTATION = "cameraOrientation";
    static final String UNIFORM_CAMERA_ADDENT = "cameraAddent";
    static final String UNIFORM_FLIP_Y = "flipY";

}
