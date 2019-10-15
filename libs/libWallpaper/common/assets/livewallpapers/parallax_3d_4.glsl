#include <precision_header.glsl>
#include <func_blend_src_over.glsl>

uniform float densityRatio;
uniform vec2 resolution;
uniform vec3 gravity;
uniform vec2 offset;
uniform sampler2D image1;
uniform ivec2 image1Size;
uniform sampler2D image2;
uniform sampler2D image3;
uniform sampler2D image4;

#include <parallax_3d_common.glsl>

void main(void)
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;

    vec4 color1 = texture2D(image1, mapFragCoordToTextureCoord(uv, -1.0));
    vec4 color2 = texture2D(image2, mapFragCoordToTextureCoord(uv, -0.6));
    vec4 color3 = texture2D(image3, mapFragCoordToTextureCoord(uv, -0.3));
    vec4 color4 = texture2D(image4, mapFragCoordToTextureCoord(uv, -0.1));

    vec4 mixed = blendSrcOver(blendSrcOver(blendSrcOver(color1, color2), color3), color4);
	gl_FragColor = mixed;
}
