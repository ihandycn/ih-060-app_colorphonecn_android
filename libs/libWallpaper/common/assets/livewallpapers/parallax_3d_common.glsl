/* A common function and constants used in parallax 3D wallpaper shader. */
uniform int flipY;
uniform vec4 sensitivity;
uniform float offsetRatio;
const float FULL_OFFSET_RATIO = 0.1;
const float G = 9.8;

vec2 mapFragCoordToTextureCoord(vec2 normFragCoord, float gravityCoef)
{
    vec2 fragCoord = normFragCoord;
    if (flipY > 0) {
        fragCoord = vec2(normFragCoord.x, 1.0 - normFragCoord.y);
    }
    float fullOffsetRatio = FULL_OFFSET_RATIO * offsetRatio;
    vec2 image1SizeF = vec2(image1Size);
    vec2 fullOffset = vec2(image1SizeF.x * fullOffsetRatio, image1SizeF.y * fullOffsetRatio);
    vec2 effectiveSize = image1SizeF - 2.0 * fullOffset;

    float fragRatio = resolution.x / resolution.y;
    float textureEffectiveRatio = effectiveSize.x / effectiveSize.y;

    vec2 visibleSize;
    if (/* LIKELY */ textureEffectiveRatio > fragRatio)
    {
        visibleSize.x = effectiveSize.y * fragRatio;
        visibleSize.y = effectiveSize.y;
    }
    else
    {
        visibleSize.x = effectiveSize.x;
        visibleSize.y = effectiveSize.x / fragRatio;
    }

    vec2 leftTop = (effectiveSize - visibleSize) * vec2(offset.x, 0.5) + fullOffset;

    vec2 clampedGravity = clamp(gravity.xy / G, vec2(-1.0, -1.0), vec2(1.0, 1.0));
    vec2 gravityOffset = clampedGravity * gravityCoef * fullOffset;
    if (flipY > 0) {
        gravityOffset = vec2(gravityOffset.x, -gravityOffset.y);
    }
    leftTop += gravityOffset;

    vec2 currentPixel = leftTop + fragCoord * visibleSize;
    return currentPixel / image1SizeF;
}
