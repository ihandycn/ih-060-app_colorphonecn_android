#include <precision_header.glsl>
#include <func_blend_src_over.glsl>

uniform float densityRatio;
uniform vec2 resolution;
uniform vec3 gravity;
uniform vec2 offset;
uniform sampler2D image1;
uniform ivec2 image1Size;
uniform sampler2D image2;

#include <parallax_3d_common.glsl>

void main(void)
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;

    vec4 baseColor = texture2D(image1, mapFragCoordToTextureCoord(uv, -1.0));
    vec4 topColor = texture2D(image2, mapFragCoordToTextureCoord(uv,-0.2));

	vec4 mixed = blendSrcOver(baseColor, topColor);

	gl_FragColor = mixed;
}
