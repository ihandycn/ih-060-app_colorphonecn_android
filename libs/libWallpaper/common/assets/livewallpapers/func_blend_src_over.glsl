/* Color blending function in SRC_OVER mode. */
vec4 blendSrcOver(vec4 dst, vec4 src)
{
    return mix(dst, src, src.a);
}
