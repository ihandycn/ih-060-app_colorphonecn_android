#include <precision_header.glsl>
#include <func_blend_src_over.glsl>

uniform float time;
uniform vec2 resolution;
uniform ivec2 image1Size;
uniform ivec2 image2Size;
uniform sampler2D image1;
uniform sampler2D image2;

const float TREE_BRANCH_BOTTOM_RATIO = 0.65;
const float TREE_BRANCH_WIDTH_RATIO = 0.65;

const float TREE_BRANCH_SHAKE_AMPLITUDE = 0.04;
const float TREE_BRANCH_SHAKE_AMPLITUDE_DISTURB = 0.03;
const float TREE_BRANCH_SHAKE_ROTATIONAL_VELOCITY = 1.6;
const float TREE_BRANCH_SHAKE_ROTATIONAL_VELOCITY_DISTURB = 0.23;

vec2 applyWind(vec2 original)
{
    float spatial = pow(original.x, 2.0);

    float majorTemporal = sin(TREE_BRANCH_SHAKE_ROTATIONAL_VELOCITY * time);
    float disturbTemporal = sin(TREE_BRANCH_SHAKE_ROTATIONAL_VELOCITY_DISTURB * time + 20.37);

    float majorOffset = spatial * majorTemporal * TREE_BRANCH_SHAKE_AMPLITUDE;
    float disturbance = spatial * disturbTemporal * TREE_BRANCH_SHAKE_AMPLITUDE_DISTURB;

    return vec2(original.x, original.y + majorOffset + disturbance);
}

vec4 getBackgroundColor(vec2 normFragCoord)
{
    vec2 imageSize = vec2(image1Size);
    float fragRatio = resolution.x / resolution.y;
    float textureRatio = imageSize.x / imageSize.y;

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

    vec2 leftTop = (imageSize - visibleSize) * 0.5;

    vec2 currentPixel = leftTop + normFragCoord * visibleSize;
    vec2 st = currentPixel / imageSize;

    return texture2D(image1, vec2(st.x, 1.0 - st.y));
}

vec4 getTreeBranchColor(vec2 normFragCoord)
{
    vec2 treeBranchSize = vec2(image2Size);
    float fragRatio = resolution.x / resolution.y;
    float textureRatio = treeBranchSize.x / treeBranchSize.y;

    float normTextureCoordX = normFragCoord.x / TREE_BRANCH_WIDTH_RATIO;
    float targetWidth = resolution.x * TREE_BRANCH_WIDTH_RATIO;
    float targetHeight = targetWidth / textureRatio;
    float normTextureCoordY = (gl_FragCoord.y - (resolution.y * TREE_BRANCH_BOTTOM_RATIO)) / targetHeight;

    if (   0.0 <= normTextureCoordX && normTextureCoordX < 1.0
        && 0.0 <= normTextureCoordY && normTextureCoordY < 1.0)
    {
        vec2 st = applyWind(vec2(normTextureCoordX, normTextureCoordY));
        return texture2D(image2, st);
    }
    return vec4(0.0, 0.0, 0.0, 0.0);
}

void main(void)
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = blendSrcOver(getBackgroundColor(uv), getTreeBranchColor(uv));
}
