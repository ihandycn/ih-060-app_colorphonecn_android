#include <precision_header.glsl>

uniform vec2 resolution;
uniform ivec2 image1Size;
uniform sampler2D image1;
uniform int flipY;

vec4 getColor(vec2 normFragCoord)
{
    vec2 fragCoord = normFragCoord;
    if (flipY > 0) {
        fragCoord = vec2(normFragCoord.x, 1.0 - normFragCoord.y);
    }
    vec2 imageSize = vec2(image1Size);
    float fragRatio = resolution.x / resolution.y;
    float textureRatio = float(image1Size.x) / float(image1Size.y);

    vec2 visibleSize;
    if (textureRatio > fragRatio)
    {
        visibleSize.x = imageSize.y * fragRatio;
        visibleSize.y = imageSize.y;
    }
    else
    {
        visibleSize.x = imageSize.x;
        visibleSize.y = imageSize.x / fragRatio;
    }

    vec2 leftTop = (imageSize - visibleSize) * vec2(0.5, 0.5);

    vec2 currentPixel = leftTop + fragCoord * visibleSize;
    vec2 st = currentPixel / imageSize;

    return texture2D(image1, st);
}

void main(void)
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = getColor(uv);
}
