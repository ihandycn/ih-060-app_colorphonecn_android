#include <precision_header.glsl>

uniform float time;
uniform float densityRatio;
uniform vec2 resolution;
uniform sampler2D image1;
uniform sampler2D image2;

const int LAYER_COUNT = 2;

const float ZOOMING_ONLY_DURATION = 5.0;
const float TRANSITION_DURATION = 1.0;
const float ONE_IMAGE_DURATION = ZOOMING_ONLY_DURATION + TRANSITION_DURATION;
const float ONE_IMAGE_ZOOM_DURATION = ZOOMING_ONLY_DURATION + 2.0 * TRANSITION_DURATION;
const float CYCLE_DURATION = ONE_IMAGE_DURATION * float(LAYER_COUNT);

/*
 *                     1 1  1   1                    2 2  2   2                    3 3  3   3
 * 11111111111111111111          22222222222222222222          33333333333333333333          111 ...
 *                     2   2  2 2                    3   3  3 3                    1   1  1 1
 * |                  |          |                  |          |                  |
 * |   ZOOM ONLY      |TRANSITION|    ZOOM ONLY     |TRANSITION|    ZOOM ONLY     |
 * |                             |                  |          |                  |
 * |     ONE IMAGE DURATION      | <--- image start |          |                  |
 * |                                                           |                  |
 * |                  | <----- ONE IMAGE ZOOM DURATION ------> |                  |
 * |                                                                              |
 * | <---------------------------- CYCLE DURATION ------------------------------> |
 */

const float ZOOM_MAX_SCALE = 1.3;

#include <background_switch_common.glsl>

int increment(int val, int foldAt)
{
    int nextVal = val + 1;
    if (nextVal >= foldAt)
    {
        return 0;
    }
    else
    {
        return nextVal;
    }
}

vec4 getColorWithZoom(int index, float zoomFraction, vec2 normFragCoord)
{
    vec2 st = getTextureCoordWithZoom(zoomFraction, normFragCoord);

    if (index == 0) return texture2D(image1, st);
    if (index == 1) return texture2D(image2, st);
    return texture2D(image1, st);
}

void main(void)
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;

    float timeSinceCycleStart = mod(time, CYCLE_DURATION);
    float timeSinceImageStart = mod(timeSinceCycleStart, ONE_IMAGE_DURATION);
    int imageIndex = int(floor(timeSinceCycleStart / ONE_IMAGE_DURATION));

    float zoomFraction = (timeSinceImageStart + TRANSITION_DURATION) / ONE_IMAGE_ZOOM_DURATION;
    if (timeSinceImageStart < ZOOMING_ONLY_DURATION)
    { // Zoom-only phase
        gl_FragColor = getColorWithZoom(imageIndex, zoomFraction, uv);
    }
    else
    { // Transition phase
        float transitionFraction = (timeSinceImageStart - ZOOMING_ONLY_DURATION) / TRANSITION_DURATION;
        float inZoomFraction = (timeSinceImageStart - ZOOMING_ONLY_DURATION) / ONE_IMAGE_ZOOM_DURATION;
        int outImageIndex = imageIndex;
        int inImageIndex = increment(imageIndex, LAYER_COUNT);
        vec4 outColor = getColorWithZoom(outImageIndex, zoomFraction, uv);
        vec4 inColor = getColorWithZoom(inImageIndex, inZoomFraction, uv);
        gl_FragColor = mix(outColor, inColor, transitionFraction);
    }
}
